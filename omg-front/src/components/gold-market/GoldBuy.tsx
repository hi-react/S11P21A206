import { useContext, useState } from 'react';

import { SocketContext } from '@/utils/SocketContext';

import Button from '../common/Button';
import LineChart from './LineChart';
import PossessionChart from './PossessionChart';

export default function GoldBuy() {
  // TODO: 서버로 부터 데이터 받아오기

  const { purchaseGold } = useContext(SocketContext);

  const MAX_TRADE_COUNT = 5; // 최대 거래 가능 수량
  const MY_MONEY = 60; // 보유한 현금

  const GOLD_PRICE = 20; // (임시) 현재 금 가격
  const GOLD_UPDOWN = 4; // (임시) 이전 타임 대비 등락

  // 선택한 금 개수
  const [goldCount, setGoldCount] = useState(0);

  // back에 보낼 6짜리 count 배열 세팅
  const handleGoldCount = (value: number) => {
    const newGoldCount = goldCount + value;

    // 최대 거래 가능 수량 넘으면 alert
    if (newGoldCount > MAX_TRADE_COUNT) {
      alert(`최대 거래 가능 수량은 ${MAX_TRADE_COUNT}개 입니다.`);
      return;
    }

    // 보유 현금 초과하면 alert
    if (GOLD_PRICE * newGoldCount > MY_MONEY) {
      alert(`보유한 현금 $${MY_MONEY}을 초과할 수 없습니다.`);
      return;
    }

    setGoldCount(Math.max(0, newGoldCount)); // 수량이 0보다 작아지지 않도록
  };

  // 매입 버튼: 서버로 goldCount 전송 !
  const handleBuying = () => {
    if (goldCount <= 0) {
      alert('금은 0개 이상 매입해야 합니다.');
      return;
    }

    purchaseGold(goldCount);
  };

  return (
    <div className='flex w-full h-full'>
      {/* 금 시세 차트 등 & 플레이어 별 지분 */}
      <section className='w-[50%] flex flex-col justify-center items-center'>
        {/* 금 시세 차트 & 현재 금 가격 & 등락 */}
        <div className='flex justify-center flex-col items-center w-full bg-skyblue h-[70%]'>
          <LineChart />
          <p>(임시) 현재 금 가격: ${GOLD_PRICE}</p>
          <p>(임시) 등락: ${GOLD_UPDOWN}</p>
        </div>

        {/* 플레이어 별 지분 차트 */}
        <div className='flex justify-center items-center w-full bg-blue h-[30%]'>
          <PossessionChart />
        </div>
      </section>

      {/* 구매 영역 */}
      <section className='w-[50%] flex justify-center items-center p-10 bg-white'>
        <div className='flex flex-col items-center justify-center w-full h-full gap-10 bg-skyblue'>
          {/* 금 이미지  */}
          <p>금 이미지</p>

          {/* 수량 선택 */}
          <div className='flex items-center'>
            <Button text='-' type='count' onClick={() => handleGoldCount(-1)} />
            <p className='mx-4 text-omg-18'>{goldCount}</p>
            <Button text='+' type='count' onClick={() => handleGoldCount(1)} />
          </div>

          {/* 총 가격 표시 */}
          <div className='flex justify-between w-full px-20 text-omg-18'>
            <p>총 가격:</p>
            <p>${GOLD_PRICE * goldCount}</p>
          </div>

          {/* 매입 버튼 */}
          <Button text='매입하기' type='trade' onClick={handleBuying} />
        </div>
      </section>
    </div>
  );
}
