package com.ddang.ddang.chat.domain.fixture;

import java.util.HashMap;
import java.util.Map;

import static com.ddang.ddang.websocket.handler.dto.WebSocketAttributeKey.BASE_URL;
import static com.ddang.ddang.websocket.handler.dto.WebSocketAttributeKey.CONNECTED;
import static com.ddang.ddang.websocket.handler.dto.WebSocketAttributeKey.USER_ID;

@SuppressWarnings("NonAsciiCharacters")
public class WebSocketSessionsTestFixture {

    protected Long 사용자_아이디 = 1L;
    protected Map<String, Object> 세션_attribute_정보 = new HashMap<>(
            Map.of(USER_ID.getName(), 사용자_아이디, BASE_URL.getName(), "/images", CONNECTED.getName(), true)
    );
    protected Long 채팅방_아이디 = 1L;
}
