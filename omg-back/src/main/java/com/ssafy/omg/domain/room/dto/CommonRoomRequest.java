package com.ssafy.omg.domain.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommonRoomRequest {
    private String gameId;
    private String sender;
    private String message;
}
