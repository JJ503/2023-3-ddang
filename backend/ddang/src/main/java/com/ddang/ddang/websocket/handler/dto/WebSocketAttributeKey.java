package com.ddang.ddang.websocket.handler.dto;

import lombok.Getter;

@Getter
public enum WebSocketAttributeKey {

    USER_ID("userId"),
    BASE_URL("baseUrl"),
    CONNECTED("connected"),
    TYPE("type");

    private final String name;

    WebSocketAttributeKey(final String name) {
        this.name = name;
    }
}
