import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import { useRoomStore } from '@/stores/room';
import { CompatClient, Frame, Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export default function Waiting() {
  const navigate = useNavigate();
  const { roomId, sender } = useRoomStore();
  const client = useRef<CompatClient | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const socket_url = import.meta.env.VITE_APP_SOCKET_URL;

  useEffect(() => {
    const socket = new SockJS(socket_url);
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, (frame: Frame) => {
      setIsConnected(true);
      console.log('Connected: ' + frame);

      // 구독 설정
      stompClient.subscribe(`/sub/${roomId}/room`, message => {
        if (message.body) {
          console.log('Received message:', JSON.parse(message.body));
        }
      });

      // 메시지 전송
      stompClient.send(
        '/pub/room',
        {},
        JSON.stringify({
          roomId,
          sender,
          message: 'LEAVE_ROOM',
        }),
      );
    });

    client.current = stompClient;

    return () => {
      if (client.current) {
        client.current.disconnect(() => {
          setIsConnected(false);
          console.log('Disconnected');
        });
      }
    };
  }, [roomId, sender]);

  const handleClick = () => {
    navigate('/game');
  };

  return (
    <div>
      <h2 className='text-lime-700'>Waiting</h2>
      <button onClick={handleClick}>START</button>
    </div>
  );
}
