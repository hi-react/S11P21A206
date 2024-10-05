import { useContext, useState } from 'react';

import { itemNameList } from '@/assets/data/stockMarketData';
import BackButton from '@/components/common/BackButton';
import Button from '@/components/common/Button';
import ChatButton from '@/components/common/ChatButton';
import ExitButton from '@/components/common/ExitButton';
import Round from '@/components/common/Round';
import Snowing from '@/components/common/Snowing';
import SpeechBubble from '@/components/common/SpeechBubble';
import Timer from '@/components/common/Timer';
import Item from '@/components/stock-market/Item';
import { treeItemNameInKorean } from '@/hooks/useStock';
import { useGameStore } from '@/stores/useGameStore';
import useModalStore from '@/stores/useModalStore';
import { SocketContext } from '@/utils/SocketContext';
import { Html, OrbitControls } from '@react-three/drei';
import { Canvas } from '@react-three/fiber';

export default function MyRoom() {
  const { modals, closeModal } = useModalStore();
  const { roundTimer, presentRound } = useContext(SocketContext);
  const { gameData } = useGameStore();

  const { tradableStockCnt } = gameData || {};

  const MAX_TRADE_COUNT = tradableStockCnt; // 최대 거래 가능 수량
  const STOCK_MARKET_PRICE = [0, 4, 6, 8, 10, 12]; // 현재 주가
  const MY_STOCK = [0, 0, 1, 0, 2, 3]; // 보유 주식 개수

  // 선택된 아이템 개수 저장하는 배열 (6 크기, 0번 인덱스는 사용 안 함)
  const [selectedCounts, setSelectedCounts] = useState(Array(6).fill(0));

  const [alertText, setAlertText] =
    useState<string>('판매 할 아이템을 선택해주세요!');

  // 보유한 아이템들만 필터링
  const ownedStockItems = MY_STOCK.slice(1)
    .map((count, index) => {
      if (count > 0) {
        return {
          itemName: itemNameList[index],
          count,
        };
      }
      return null;
    })
    .filter(Boolean);

  // 가로로 중앙 정렬을 위한 위치 계산
  const spacing = 1.5; // 아이템 간격
  const startPosition = -(ownedStockItems.length - 1) * (spacing / 2);

  // 예상 판매 수익 계산 함수
  const calculateTotalRevenue = () => {
    return selectedCounts.reduce(
      (total, count, index) => total + count * STOCK_MARKET_PRICE[index],
      0,
    );
  };

  const handleCountChange = (itemIndex: number, value: number) => {
    const newCounts = [...selectedCounts];
    const stockIndex = itemNameList.indexOf(
      ownedStockItems[itemIndex].itemName,
    ); // 필터링된 아이템의 원래 인덱스 찾기

    const newCount = newCounts[stockIndex + 1] + value;

    // 총 선택한 수량 계산
    const totalSelectedCount =
      newCounts.reduce((acc, count) => acc + count, 0) + value;

    if (totalSelectedCount > MAX_TRADE_COUNT) {
      setAlertText(`(현재) 최대 판매 가능 수량은 ${MAX_TRADE_COUNT}개 입니다.`);
      return;
    }

    if (newCount > MY_STOCK[stockIndex + 1]) {
      const itemName = treeItemNameInKorean(itemNameList[stockIndex]); // 해당 아이템 이름 가져오기
      setAlertText(
        `'${itemName}' 보유 수량 ${MY_STOCK[stockIndex + 1]}개를 초과할 수 없습니다.`,
      );
      return;
    }

    newCounts[stockIndex + 1] = Math.max(0, newCount); // 수량이 0보다 작아지지 않도록
    setSelectedCounts(newCounts);

    // 선택한 아이템과 수량 알림 추가
    const selectedItemName = treeItemNameInKorean(itemNameList[stockIndex]);
    setAlertText(
      `${selectedItemName}을(를) ${newCounts[stockIndex + 1]}개 선택했습니다.`,
    );
  };

  const goToSellStockItem = () => {
    console.log('판매 할 수량: ', selectedCounts);
  };

  const handleCloseMyRoom = (e: React.MouseEvent<HTMLDivElement>) => {
    if (e.target === e.currentTarget && modals.myRoom) {
      closeModal('myRoom');
    }
  };

  // 뒤로 가기 버튼
  const handleBackButton = () => {
    if (modals.myRoom) {
      closeModal('myRoom');
    }
  };

  return (
    <div
      className='fixed top-0 left-0 z-50 flex items-center justify-center w-full h-full'
      onClick={handleCloseMyRoom}
    >
      <main className='relative w-full h-screen bg-center bg-cover bg-skyblue'>
        {/* Header: 뒤로 가기 & Round-Timer 고정 렌더링 */}
        <section className='absolute top-0 left-0 z-10 flex items-start justify-between w-full px-10 py-10 text-black text-omg-40b'>
          <BackButton onClick={handleBackButton} />
          <div className='flex flex-col items-end gap-4'>
            <Round presentRound={presentRound} />
            <Timer time={roundTimer} />
          </div>
        </section>

        {/* 말풍선 */}
        <section className='absolute z-10 flex -translate-x-1/2 left-1/2 top-32'>
          <SpeechBubble text={alertText} />
        </section>

        {/* Footer: 채팅 & 종료 버튼 고정 렌더링 */}
        <section className='absolute bottom-0 left-0 z-10 flex items-center justify-between w-full text-black py-14 px-14 text-omg-40b'>
          <ChatButton />
          <ExitButton />
        </section>

        <Canvas
          style={{ height: '100vh', width: '100vw' }}
          camera={{ position: [0, 5, 20], fov: 20 }} // 카메라 위치와 fov 설정
        >
          {/* 카메라 컨트롤 */}
          <OrbitControls
            enablePan={false} // 팬(이동) 금지
            enableZoom={false} // 줌 금지
            enableRotate={false} // 회전 금지
          />
          {/* 조명 설정 */}
          <ambientLight intensity={5} />
          <directionalLight position={[5, 5, 5]} intensity={7} />
          <pointLight position={[0, 10, 0]} intensity={3} />
          <Snowing />

          {ownedStockItems.map((item, itemIndex) => {
            const positionX = startPosition + itemIndex * spacing;
            const stockIndex = itemNameList.indexOf(item.itemName); // 필터링된 아이템의 원래 인덱스 찾기

            return (
              <group key={item.itemName}>
                <Item
                  itemName={item.itemName}
                  position={{ x: positionX, y: 0.8, z: 0 }} // X축으로 위치 조정
                  onClick={() => console.log(`${item.itemName} 클릭됨`)}
                  disabled={false}
                />
                {/* 보유 수량 & 판매할 수량 선택 & 현재 판매가 */}
                <Html position={[positionX, 0, 0]} center>
                  <div className='flex flex-col items-center w-40 gap-2 text-omg-18'>
                    {/* 보유 수량 */}
                    <div>보유 수량: {item.count}개</div>

                    {/* 수량 선택 */}
                    <div className='flex items-center'>
                      <Button
                        text='-'
                        type='count'
                        onClick={() => handleCountChange(itemIndex, -1)}
                        disabled={selectedCounts[stockIndex + 1] === 0}
                      />
                      <p className='mx-4'>{selectedCounts[stockIndex + 1]}개</p>
                      <Button
                        text='+'
                        type='count'
                        onClick={() => handleCountChange(itemIndex, 1)}
                      />
                    </div>

                    {/* 현재 판매가 */}
                    <div className='text-omg-14'>
                      현재 판매가: ${STOCK_MARKET_PRICE[stockIndex + 1]}
                    </div>
                  </div>
                </Html>
              </group>
            );
          })}
        </Canvas>

        <div className='absolute flex flex-col items-center gap-4 -translate-x-1/2 text-omg-18 bottom-56 left-1/2'>
          <p>예상 판매 수익: ${calculateTotalRevenue()}</p>
          <Button
            text='주식 시장에 팔러 가기'
            type='trade'
            onClick={goToSellStockItem}
          />
        </div>
      </main>
    </div>
  );
}
