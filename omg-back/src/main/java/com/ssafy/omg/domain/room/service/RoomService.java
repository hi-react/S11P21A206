package com.ssafy.omg.domain.room.service;

import com.ssafy.omg.domain.room.dto.CommonRoomRequest;
import com.ssafy.omg.domain.room.dto.CommonRoomResponse;

public interface RoomService {
    // 대기 방 입장
    CommonRoomResponse enterRoom(CommonRoomRequest request);

    // 대기 방 나가기
    CommonRoomResponse leaveRoom(CommonRoomRequest request);

    // 시작 버튼 클릭
    CommonRoomResponse startButtonClicked(CommonRoomRequest request);

    // 플레이어 렌더링 완료
    CommonRoomResponse renderedComplete(CommonRoomRequest request);
}
