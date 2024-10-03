import { useContext, useEffect } from 'react';

import { itemNameList } from '@/assets/data/stockMarketData';
import { getTreeItemImagePath } from '@/hooks/useStock';
import { useGameStore } from '@/stores/useGameStore';
import { useSocketMessage } from '@/stores/useSocketMessage';
import { useStockStore } from '@/stores/useStockStore';
import { SocketContext } from '@/utils/SocketContext';

import Button from '../common/Button';
import PossessionChart from './PossessionChart';

export default function StockBuy() {
  const { buyStock } = useContext(SocketContext);
  const { stockPrices, leftStocks } = useStockStore();

  const { gameData, selectedCount, setSelectedCount } = useGameStore();
  const { buyStockMessage } = useSocketMessage();

  const { tradableStockCnt } = gameData || {};

  const MAX_TRADE_COUNT = tradableStockCnt || 1;
  // TODO: 돈 정보 받아와야 함
  const MY_MONEY = 50;

  useEffect(() => {
    if (!buyStockMessage.message) return;

    alert(buyStockMessage.message);
  }, [buyStockMessage]);

  // 총 선택된 수량 계산
  const totalSelectedCount = selectedCount.reduce(
    (acc, count) => acc + count,
    0,
  );

  const handleCountChange = (idx: number, value: number) => {
    // 현재 수량 업데이트
    const updatedCounts = [...selectedCount];
    const newCount = updatedCounts[idx + 1] + value;

    // 수량이 0보다 작아지지 않도록
    if (newCount < 0) return;

    // 최대 거래 가능 수량 넘으면 alert
    if (totalSelectedCount + value > MAX_TRADE_COUNT) {
      alert(`최대 거래 가능 수량은 ${MAX_TRADE_COUNT}개 입니다.`);
      return;
    }

    // 보유 현금 초과하면 alert
    const newTotalPrice = totalPrice + value * stockPrices[idx + 1];
    if (newTotalPrice > MY_MONEY) {
      alert(`보유한 현금 $${MY_MONEY}을 초과할 수 없습니다.`);
      return;
    }

    updatedCounts[idx + 1] = newCount;
    setSelectedCount(updatedCounts);
  };

  // 총 가격 계산
  const totalPrice = selectedCount.reduce(
    (acc, count, idx) => acc + count * stockPrices[idx],
    0,
  );

  const handleBuying = () => {
    buyStock(selectedCount);
    setSelectedCount([0, 0, 0, 0, 0, 0]);
  };

  return (
    <div className='flex justify-between w-full h-full'>
      {/* 주식 별 지분 */}
      <section className='flex justify-between w-[50%]'>
        <PossessionChart />
      </section>

      {/* 주식 구매 영역 */}
      <section className='w-[50%] flex justify-center items-center'>
        <div className='flex flex-col w-full h-full gap-10 px-20 py-10'>
          {/* 이미지 & 주가 & 남은 수량 & 수량 선택 */}
          <ul className='flex flex-col gap-8'>
            {itemNameList.map((treeItem, idx) => (
              <li
                key={idx}
                className='flex items-center justify-between text-omg-18'
              >
                {/* 이미지 */}
                <div className='w-10 h-10'>
                  <img
                    src={getTreeItemImagePath(treeItem)}
                    className='object-contain w-full h-full'
                  />
                </div>
                {/* 주가 */}
                <p>${stockPrices[idx + 1]}</p>
                {/* 남은 수량 */}
                <p className='text-omg-14'>
                  남은 수량: {leftStocks[idx + 1]}개
                </p>
                {/* 수량 선택 */}
                <div className='flex items-center'>
                  <Button
                    text='-'
                    type='count'
                    onClick={() => handleCountChange(idx, -1)}
                    disabled={selectedCount[idx + 1] === 0}
                  />
                  <p className='mx-4 text-omg-18'>{selectedCount[idx + 1]}</p>
                  <Button
                    text='+'
                    type='count'
                    onClick={() => handleCountChange(idx, 1)}
                  />
                </div>
              </li>
            ))}
          </ul>

          {/* 총 가격 표시 */}
          <div className='flex justify-between text-omg-18'>
            <p>총 가격:</p>
            <p>${totalPrice}</p>
          </div>

          {/* 매수 버튼 */}
          <div className='flex items-center justify-center'>
            <Button text='매수하기' type='trade' onClick={handleBuying} />
          </div>
        </div>
      </section>
    </div>
  );
}
