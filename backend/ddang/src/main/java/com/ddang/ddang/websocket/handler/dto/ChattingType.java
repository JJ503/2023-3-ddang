package com.ddang.ddang.websocket.handler.dto;

import com.ddang.ddang.websocket.handler.exception.UnsupportedChattingTypeException;

import java.util.Arrays;

public enum ChattingType {

    MESSAGE,
    PING;

    public static ChattingType findValue(final String value) {
        return Arrays.stream(ChattingType.values())
                     .filter(chattingType -> chattingType.name().equalsIgnoreCase(value))
                     .findFirst()
                     .orElseThrow(() -> new UnsupportedChattingTypeException("잘못된 채팅 타입입니다."));
    }
}
