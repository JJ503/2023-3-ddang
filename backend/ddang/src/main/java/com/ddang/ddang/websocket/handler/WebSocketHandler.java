package com.ddang.ddang.websocket.handler;

import com.ddang.ddang.websocket.handler.dto.SendMessageDto;
import com.ddang.ddang.websocket.handler.dto.TextMessageDto;
import com.ddang.ddang.websocket.handler.dto.TextMessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private static final String TYPE_KEY = "type";

    private final WebSocketHandleTextMessageProviderComposite providerComposite;
    private final ObjectMapper objectMapper;

    @Override
    protected void handleTextMessage(final WebSocketSession session, final TextMessage message) throws Exception {
        final String payload = message.getPayload();
        final TextMessageDto textMessageDto = objectMapper.readValue(payload, TextMessageDto.class);
        session.getAttributes().put(TYPE_KEY, textMessageDto.type());
        log.info("handle text message: {}, {}, ", session.getId(), textMessageDto);

        final WebSocketHandleTextMessageProvider provider = providerComposite.findProvider(textMessageDto.type());
        final List<SendMessageDto> sendMessageDtos = provider.handleCreateSendMessage(session, textMessageDto.data());
        for (SendMessageDto sendMessageDto : sendMessageDtos) {
            sendMessageDto.session()
                          .sendMessage(sendMessageDto.textMessage());
            System.out.println(sendMessageDto.session() + "의 연결 상태에서 ping 보내기");
            sendMessageDto.session()
                          .sendMessage(new PingMessage());
        }
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        System.out.println("session: " + session + ", pong: " + message);
    }

    @Override
    public void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) throws Exception {
        final String type = String.valueOf(session.getAttributes().get(TYPE_KEY));
        final TextMessageType textMessageType = TextMessageType.valueOf(type);
        final WebSocketHandleTextMessageProvider provider = providerComposite.findProvider(textMessageType);
        provider.remove(session);
        log.info("closed connection: {}, {}", session.getId(), session.getAttributes().get("userId"));
    }
}
