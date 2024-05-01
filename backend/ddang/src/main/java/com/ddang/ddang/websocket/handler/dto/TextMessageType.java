package com.ddang.ddang.websocket.handler.dto;

import com.ddang.ddang.websocket.handler.exception.UnsupportedTextMessageTypeException;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum TextMessageType {

    CHATTINGS,
    BIDS;

    @JsonCreator
    public static TextMessageType fromString(final String value) {
        return Arrays.stream(TextMessageType.values())
                     .filter(messageType -> messageType.name().equalsIgnoreCase(value))
                     .findFirst()
                     .orElseThrow(() -> new UnsupportedTextMessageTypeException("잘못된 메시지 타입입니다."));
    }
}
