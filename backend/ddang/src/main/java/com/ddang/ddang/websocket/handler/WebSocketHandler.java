package com.ddang.ddang.websocket.handler;

import com.ddang.ddang.websocket.handler.dto.SendMessageDto;
import com.ddang.ddang.websocket.handler.dto.TextMessageDto;
import com.ddang.ddang.websocket.handler.dto.TextMessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;

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

        final WebSocketHandleTextMessageProvider provider = providerComposite.findProvider(textMessageDto.type());
        final List<SendMessageDto> sendMessageDtos = provider.handleCreateSendMessage(session, textMessageDto.data());
        for (SendMessageDto sendMessageDto : sendMessageDtos) {
            sendMessageDto.session()
                          .sendMessage(sendMessageDto.textMessage());
        }
    }

    @Override
    public void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) {
        final String type = String.valueOf(session.getAttributes().get(TYPE_KEY));
        final TextMessageType textMessageType = TextMessageType.valueOf(type);
        final WebSocketHandleTextMessageProvider provider = providerComposite.findProvider(textMessageType);
        provider.remove(session);
    }

    @Override
    public void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        log.info("pong 수신 : {} ", session);
        final Map<String, Object> attributes = session.getAttributes();
        attributes.put("ping status", true);
    }
}
