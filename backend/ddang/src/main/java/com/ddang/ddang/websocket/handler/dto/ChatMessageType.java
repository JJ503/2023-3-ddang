package com.ddang.ddang.websocket.handler.dto;

import java.util.Arrays;

public enum ChatMessageType {

    MESSAGE("message"),
    PING("ping"),
    ;

    private final String value;

    ChatMessageType(final String value) {
        this.value = value;
    }

    public static ChatMessageType findMessageType(final String value) {
        return Arrays.stream(ChatMessageType.values())
                .filter(chattingType -> chattingType.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("잘못된 채팅 타입입니다."));
    }
}
