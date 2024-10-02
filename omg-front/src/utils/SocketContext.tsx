import { ReactNode, createContext, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import { useGameStore } from '@/stores/useGameStore';
import { useOtherUserStore } from '@/stores/useOtherUserState';
import { useSocketMessage } from '@/stores/useSocketMessage';
import useUser from '@/stores/useUser';
import type { ChatMessage, Player } from '@/types';
import { Client } from '@stomp/stompjs';

interface SocketContextType {
  socket: Client | null;
  online: boolean;
  player: string[];
  chatMessages: ChatMessage[];
  connect: () => void;
  disconnect: () => void;
  roomSubscription: () => void;
  leaveRoom: () => void;
  chatSubscription: () => void;
  sendMessage: (msg: string) => void;
  hostPlayer: string | null;
  startGame: () => void;
  rendered_complete: () => void;
  gameSubscription: () => void;
  players: Player[];
  movePlayer: (
    position: number[],
    direction: number[],
    actionToggle: boolean,
  ) => void;
  initGameSetting: () => void;
  allRendered: boolean;
  purchaseGold: (goldPurchaseCount: number) => void;
  takeLoan: (loanAmount: number) => void;
  repayLoan: (repayLoanAmount: number) => void;
  buyStock: (stocks: number[]) => void;
  sellStock: (stocks: number[]) => void;
}

const defaultContextValue: SocketContextType = {
  socket: null,
  online: false,
  player: [],
  chatMessages: [],
  connect: () => {},
  disconnect: () => {},
  roomSubscription: () => {},
  leaveRoom: () => {},
  chatSubscription: () => {},
  sendMessage: () => {},
  hostPlayer: '',
  startGame: () => {},
  rendered_complete: () => {},
  gameSubscription: () => {},
  players: [],
  movePlayer: () => {},
  initGameSetting: () => {},
  allRendered: false,
  purchaseGold: () => {},
  takeLoan: () => {},
  repayLoan: () => {},
  buyStock: () => {},
  sellStock: () => {},
};

export const SocketContext =
  createContext<SocketContextType>(defaultContextValue);

interface SocketProviderProps {
  children: ReactNode;
}

export default function SocketProvider({ children }: SocketProviderProps) {
  const { roomId } = useParams<{ roomId: string }>();
  const { nickname, setCharacterType, setPlayerIndex } = useUser();
  const base_url = import.meta.env.VITE_APP_SOCKET_URL;
  const {
    gameMessage,
    setRoomMessage,
    setGameMessage,
    setBuyMessage,
    setSellMessage,
    setLoanMessage,
    setRepayLoanMessage,
    setGoldPurchaseMessage,
    setEventCardMessage,
    setGameRoundMessage,
  } = useSocketMessage();
  const { setGameData } = useGameStore();
  const [socket, setSocket] = useState<Client | null>(null);
  const [online, setOnline] = useState(false);
  const [player, setPlayer] = useState<string[]>([]);
  const [players, setPlayers] = useState<Player[]>([]);
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [hostPlayer, setHostPlayer] = useState<string | null>(null);
  const [allRendered, setAllRendered] = useState(false);
  const navigate = useNavigate();

  const roomTopic = `/sub/${roomId}/room`;
  const chatTopic = `/sub/${roomId}/chat`;
  const gameTopic = `/sub/${roomId}/game`;
  const subRoomId = `room-${roomId}`;
  const subChatId = `chat-${roomId}`;
  const subGameId = `game-${roomId}`;

  const isSocketConnected = () => {
    if (!socket || !socket.connected) {
      console.log('소켓이 아직 연결되지 않았습니다.');
      return false;
    }
    return true;
  };

  const leaveRoom = () => {
    if (!isSocketConnected()) return;

    //  대기방 구독 해제
    socket.unsubscribe(subRoomId);

    const leaveMessagePayload = {
      roomId,
      sender: nickname,
      message: 'LEAVE_ROOM',
    };

    socket.publish({
      destination: '/pub/room',
      body: JSON.stringify(leaveMessagePayload),
    });
  };

  // 소켓 연결
  const connect = () => {
    const stompClient = new Client({
      brokerURL: base_url,
      debug: str => console.log(str),
      onConnect: () => {
        setSocket(stompClient);
        setOnline(true);
      },
      onDisconnect: () => {
        setSocket(null);
        setOnline(false);
      },
    });
    stompClient.activate();
  };

  // 소켓 해제
  const disconnect = () => {
    if (socket) {
      socket.deactivate();
      setSocket(null);
      setOnline(false);
    }
  };

  // 대기방 구독
  const roomSubscription = () => {
    if (!isSocketConnected()) return;

    socket.subscribe(
      roomTopic,
      message => {
        const parsedMessage = JSON.parse(message.body);
        setRoomMessage(parsedMessage);
        switch (parsedMessage.message) {
          case 'ENTER_SUCCESS':
            const playerList = parsedMessage.room.inRoomPlayers;
            const host = parsedMessage.room.hostNickname;
            const playerNicknames = playerList.map(
              (player: { nickname: string }) => player.nickname,
            );
            setPlayer(playerNicknames);
            setHostPlayer(host);
            break;
          case 'ENTER_FAILURE':
            break;
          case 'PREPARE_GAME_START':
            break;
          case 'LEAVE_ROOM':
            setPlayer(prevPlayers =>
              prevPlayers.filter(player => player !== parsedMessage.sender),
            );
            break;
          case 'START_BUTTON_CLICKED':
            console.log('호스트가 게임을 시작했습니다');
            navigate(`/game/${roomId}`);
            break;
          case 'RENDERED_COMPLETE':
            if (parsedMessage.roomId === roomId && parsedMessage.sender) {
              console.log(
                `${parsedMessage.sender} 님의 렌더링이 완료되었습니다.`,
              );
            }
            break;
          case 'ALL_RENDERED_COMPLETED':
            console.log('모든 렌더링이 완료되었습니다.');
            setAllRendered(true);
            socket.unsubscribe(subRoomId);
            gameSubscription();
            chatSubscription();
            break;
        }
      },
      { id: subRoomId },
    );
    enterRoom();
  };

  // 게임방 구독
  const gameSubscription = () => {
    if (!isSocketConnected()) return;
    socket.subscribe(
      gameTopic,
      message => {
        const parsedMessage = JSON.parse(message.body);
        const currentUser = parsedMessage.sender;
        switch (parsedMessage.type) {
          case 'GAME_INITIALIZED':
            const initGameData = parsedMessage.data.game;
            if (initGameData.gameId === nickname) {
              setGameData(initGameData);
            }
            const initPlayerData = parsedMessage.data.game.players;
            setGameMessage(initPlayerData);
            setPlayers(initPlayerData);
            useOtherUserStore.getState().setOtherUsers(
              initPlayerData.map((player: Player) => ({
                id: player.nickname,
                characterType: player.characterType,
                position: player.position,
                direction: player.direction,
              })),
            );

            const currentUserIndex = initPlayerData.findIndex(
              (player: Player) => player.nickname === nickname,
            );
            if (currentUserIndex !== -1) {
              setPlayerIndex(currentUserIndex);
            }
            const currentUserNickname = initPlayerData.find(
              (player: Player) => player.nickname === nickname,
            );

            if (currentUserNickname) {
              setCharacterType(currentUserNickname.characterType);
            }
            break;

          case 'PLAYER_STATE':
            console.log('16ms마다 들어오는 실시간 게임 정보');
            const otherPlayersData = parsedMessage.data.filter(
              (player: Player) => player.nickname !== nickname,
            );
            if (otherPlayersData.length > 0) {
              const updatedOtherUsers = otherPlayersData.map(
                (player: Player) => {
                  const existingUser = useOtherUserStore
                    .getState()
                    .otherUsers.find(user => user.id === player.nickname);

                  return {
                    id: player.nickname,
                    characterType: existingUser?.characterType || 0,
                    position: player.position,
                    direction: player.direction,
                    actionToggle: player.actionToggle,
                  };
                },
              );
              useOtherUserStore.getState().setOtherUsers(updatedOtherUsers);
            }
            break;

          case 'SUCCESS_PURCHASE_GOLD':
            if (currentUser === nickname) {
              setGoldPurchaseMessage({
                message: parsedMessage.data.goldOwned,
                isCompleted: true,
              });
            }
            break;

          case 'SUCCESS_TAKE_LOAN':
            if (currentUser === nickname) {
              setLoanMessage({
                message: parsedMessage.data.loanPrincipal,
                isCompleted: true,
              });
            }
            break;

          case 'SUCCESS_REPAY_LOAN':
            if (currentUser === nickname) {
              setRepayLoanMessage({
                message: parsedMessage.data.totalDebt,
                isCompleted: true,
              });
            }
            break;

          case 'OUT_OF_CASH':
            if (currentUser === nickname) {
              setGoldPurchaseMessage({
                message: '돈이 부족합니다.',
                isCompleted: false,
              });
            }
            break;

          case 'GOLD_ALREADY_PURCHASED':
            if (currentUser === nickname) {
              setGoldPurchaseMessage({
                message: '이미 한 라운드 내에서 금괴를 구매했습니다.',
                isCompleted: false,
              });
            }
            break;

          case 'AMOUNT_OUT_OF_RANGE':
            if (currentUser === nickname) {
              setLoanMessage({
                message: '가능한 대출한도를 넘었습니다.',
                isCompleted: false,
              });
            }
            break;

          case 'LOAN_ALREADY_TAKEN':
            if (currentUser === nickname) {
              setLoanMessage({
                message: '이미 대출을 받았습니다.',
                isCompleted: false,
              });
            }
            break;

          case 'AMOUNT_EXCEED_DEBT':
            if (currentUser === nickname) {
              setRepayLoanMessage({
                message: '상환금액이 총 부채금액 보다 큽니다.',
                isCompleted: false,
              });
            }
            break;

          case 'AMOUNT_EXCEED_CASH':
            if (currentUser === nickname) {
              setRepayLoanMessage({
                message: '상환금액이 보유 현금 자산 보다 큽니다.',
                isCompleted: false,
              });
            }
            break;

          case 'GAME_NOTIFICATION':
            if (parsedMessage.data.roundStatus === 'ECONOMIC_EVENT') {
              setEventCardMessage(parsedMessage.data);
            } else {
              setGameRoundMessage({
                type: parsedMessage.type,
                message: parsedMessage.data.message,
              });
            }
            break;

          case 'SUCCESS_BUY_STOCK':
            if (currentUser === nickname) {
              setGameData(parsedMessage.data);
              setBuyMessage({
                message: '매수 완료!',
                isCompleted: true,
              });
              console.log('매수 성공', parsedMessage.data);
            }
            break;

          case 'INSUFFICIENT_CASH':
            if (currentUser === nickname) {
              setGameData(parsedMessage.data);
              setBuyMessage({
                message: '돈이 부족합니다.',
                isCompleted: false,
              });
              console.log('돈이 부족합니다.', parsedMessage.data);
            }
            break;

          case 'STOCK_NOT_AVAILABLE':
            if (currentUser === nickname) {
              setGameData(parsedMessage.data);
              setBuyMessage({
                message: '다른 사람이 이미 구매해서 개수가 부족합니다.',
                isCompleted: false,
              });
              console.log(
                '다른 사람이 이미 구매해서 개수가 부족합니다.',
                parsedMessage.data,
              );
            }
            break;

          case 'SUCCESS_SELL_STOCK':
            if (currentUser === nickname) {
              setGameData(parsedMessage.data);
              setSellMessage({
                message: '매도 성공!',
                isCompleted: true,
              });
            }
            break;

          case 'STOCK_FLUCTUATION':
            setGameData(parsedMessage.data);
            console.log('경제상황 발생', parsedMessage.data);
            break;
        }
      },
      { id: subGameId },
    );
  };

  useEffect(() => {
    if (roomId) {
      disconnect();
      connect();
    }

    return () => {
      disconnect();
    };
  }, [roomId]);

  // 채팅방 구독
  const chatSubscription = () => {
    if (!isSocketConnected()) return;

    socket.subscribe(
      chatTopic,
      message => {
        const parsedMessage = JSON.parse(message.body);
        if (parsedMessage.data) {
          const sender = parsedMessage.data.sender;
          const content = parsedMessage.data.content;
          setChatMessages(prevMessages => [
            ...prevMessages,
            { sender, content },
          ]);
        }
      },
      { id: subChatId },
    );
  };

  // 채팅 메세지 전송
  const sendMessage = (message: string) => {
    if (!isSocketConnected()) return;

    const messagePayload = {
      type: 'CHAT',
      roomId,
      sender: nickname,
      data: {
        sender: nickname,
        content: message,
      },
    };

    socket.publish({
      destination: `/pub/${roomId}/chat`,
      body: JSON.stringify(messagePayload),
    });
  };

  // 개별 유저 렌더링 완료 전송
  const rendered_complete = () => {
    if (!isSocketConnected()) return;

    const messagePayload = {
      roomId,
      sender: nickname,
      message: 'RENDERED_COMPLETE',
    };

    socket.publish({
      destination: '/pub/room',
      body: JSON.stringify(messagePayload),
    });
  };

  // 게임시작
  const startGame = () => {
    if (!isSocketConnected()) return;

    const startMessagePayload = {
      roomId,
      sender: nickname,
      message: 'START_BUTTON_CLICKED',
    };

    socket.publish({
      destination: '/pub/room',
      body: JSON.stringify(startMessagePayload),
    });
  };

  // 초기 게임 세팅
  const initGameSetting = () => {
    if (nickname !== hostPlayer) {
      return;
    }

    const messagePayload: {
      roomId: string;
      type: string;
      sender: string;
      data: null | object;
    } = {
      type: 'test',
      roomId,
      sender: nickname,
      data: null,
    };

    socket.publish({
      destination: '/pub/game-initialize',
      body: JSON.stringify(messagePayload),
    });
  };

  // 대기방 입장
  const enterRoom = () => {
    const messagePayload = {
      roomId,
      sender: nickname,
      message: 'ENTER_ROOM',
    };

    socket.publish({
      destination: '/pub/room',
      body: JSON.stringify(messagePayload),
    });
  };

  // 캐릭터 이동
  const movePlayer = (
    position: number[],
    direction: number[],
    actionToggle: boolean,
  ) => {
    if (!isSocketConnected()) return;
    const messagePayload = {
      type: 'PLAYER_MOVE',
      roomId,
      sender: nickname,
      data: {
        position,
        direction,
        actionToggle,
      },
    };
    socket.publish({
      destination: '/pub/player-move',
      body: JSON.stringify(messagePayload),
    });
  };

  // 금괴 매입
  const purchaseGold = (goldPurchaseCount: number) => {
    if (!isSocketConnected()) return;
    const messagePayload = {
      type: 'PURCHASE_GOLD',
      roomId,
      sender: nickname,
      data: goldPurchaseCount,
    };
    socket.publish({
      destination: '/pub/gold',
      body: JSON.stringify(messagePayload),
    });
  };

  // 대출 신청
  const takeLoan = (loanAmount: number) => {
    if (!isSocketConnected()) return;
    const messagePayload = {
      type: 'TAKE_LOAN',
      roomId,
      sender: nickname,
      data: loanAmount,
    };
    socket.publish({
      destination: '/pub/take-loan',
      body: JSON.stringify(messagePayload),
    });
  };

  // 대출 상환
  const repayLoan = (repayLoanAmount: number) => {
    if (!isSocketConnected()) return;
    const messagePayload = {
      type: 'REPAY_LOAN',
      roomId,
      sender: nickname,
      data: repayLoanAmount,
    };
    socket.publish({
      destination: '/pub/repay-loan',
      body: JSON.stringify(messagePayload),
    });
  };

  // 주식 매수
  const buyStock = (stocks: number[]) => {
    if (!isSocketConnected()) return;
    const messagePayload = {
      type: 'BUY_STOCK',
      roomId,
      sender: nickname,
      data: { stocks },
    };
    socket.publish({
      destination: '/pub/buy-stock',
      body: JSON.stringify(messagePayload),
    });
  };

  // 주식 매도
  const sellStock = (stocks: number[]) => {
    if (!isSocketConnected()) return;
    const messagePayload = {
      type: 'SELL_STOCK',
      roomId,
      sender: nickname,
      data: { stocks },
    };

    socket.publish({
      destination: '/pub/sell-stock',
      body: JSON.stringify(messagePayload),
    });
  };

  const contextValue = useMemo(
    () => ({
      socket,
      online,
      player,
      chatMessages,
      connect,
      disconnect,
      roomSubscription,
      leaveRoom,
      chatSubscription,
      sendMessage,
      hostPlayer,
      startGame,
      rendered_complete,
      gameSubscription,
      players,
      movePlayer,
      initGameSetting,
      allRendered,
      purchaseGold,
      takeLoan,
      repayLoan,
      buyStock,
      sellStock,
    }),
    [
      socket,
      online,
      player,
      players,
      chatMessages,
      movePlayer,
      allRendered,
      gameMessage,
      purchaseGold,
      takeLoan,
      repayLoan,
      buyStock,
      sellStock,
    ],
  );

  return (
    <SocketContext.Provider value={contextValue}>
      {children}
    </SocketContext.Provider>
  );
}
