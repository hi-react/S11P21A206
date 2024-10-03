import { useState } from 'react';

import {
  getMaxPrice,
  stockDataUntilNow,
  treeItemNameInKorean,
} from '@/hooks/useStock';
import { StockDataItem, StockDataItemInKorean } from '@/types';
import { ResponsiveLine } from '@nivo/line';

interface LineChartProps {
  stockData: StockDataItem[];
}

export default function LineChart({ stockData }: LineChartProps) {
  const [currentRound, _] = useState(1); // 현재 라운드 설정

  // stockData의 id(StockItem) => 한글로 변환
  const stockDataWithKoreanTreeItemName: StockDataItemInKorean[] =
    stockData.map(item => ({
      ...item,
      id: treeItemNameInKorean(item.id),
    }));

  // 필터링된 데이터와 최대 주가 계산
  const filteredData = stockDataUntilNow(
    stockDataWithKoreanTreeItemName,
    currentRound,
  );
  const maxPrice = getMaxPrice(stockDataWithKoreanTreeItemName);

  return (
    <ResponsiveLine
      data={filteredData} // 현재 ROUND까지의 데이터만 활용
      margin={{ top: 10, right: 110, bottom: 80, left: 80 }}
      xScale={{ type: 'linear', min: 0, max: currentRound * 6 }} // 각 라운드가 6개의 데이터로 나뉨
      yScale={{ type: 'linear', min: 0, max: maxPrice }} // 동적으로 최대 값 조정
      axisBottom={{
        tickValues: Array.from({ length: currentRound }, (_, i) => (i + 1) * 6), // 각 라운드 끝에만 틱 값 설정 (6, 12, 18, ...)
        format: x => `${x / 6}라운드`, // x축에 라운드 단위로 표시
        legend: '게임 Round',
        legendOffset: 50,
        legendPosition: 'middle',
      }}
      axisLeft={{
        legend: '가격',
        legendOffset: -50,
        legendPosition: 'middle',
      }}
      gridXValues={Array.from({ length: currentRound }, (_, i) => (i + 1) * 6)} // x축의 그리드 라인도 라운드 끝에만 표시
      enablePoints={true}
      pointSize={10}
      pointColor={{ theme: 'background' }}
      pointBorderWidth={2}
      pointBorderColor={{ from: 'serieColor' }}
      pointLabelYOffset={-12}
      useMesh={true}
      lineWidth={3} // 라인 두께 조절
      theme={{
        grid: {
          line: {
            stroke: 'black',
          },
        },
        axis: {
          ticks: {
            text: {
              fontFamily: 'Katuri',
              fontSize: 14,
            },
          },
          legend: {
            text: {
              fontFamily: 'Katuri',
              fontSize: 14,
            },
          },
        },
        legends: {
          text: {
            fontFamily: 'Katuri',
            fontSize: 16,
          },
        },
      }}
      // 차트 범례 (아이템 항목)
      legends={[
        {
          anchor: 'bottom-right',
          direction: 'column',
          justify: false,
          translateX: 100,
          translateY: 0,
          itemsSpacing: 4,
          itemDirection: 'left-to-right',
          itemWidth: 80,
          itemHeight: 28,
          itemOpacity: 0.8,
          symbolSize: 12,
          symbolShape: 'circle',
          symbolBorderColor: 'rgba(0, 0, 0, .5)',
          effects: [
            {
              on: 'hover',
              style: {
                itemBackground: 'rgba(0, 0, 0, .03)',
                itemOpacity: 1,
              },
            },
          ],
        },
      ]}
      //
    />
  );
}
