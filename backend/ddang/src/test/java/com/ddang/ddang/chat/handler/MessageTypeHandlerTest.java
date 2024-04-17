package com.ddang.ddang.chat.handler;

import com.ddang.ddang.chat.application.event.MessageNotificationEvent;
import com.ddang.ddang.chat.application.event.UpdateReadMessageLogEvent;
import com.ddang.ddang.chat.domain.WebSocketChatSessions;
import com.ddang.ddang.chat.handler.fixture.MessageTypeHandlerTestFixture;
import com.ddang.ddang.configuration.IsolateDatabase;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.web.socket.WebSocketSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.willReturn;

@IsolateDatabase
@RecordApplicationEvents
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class MessageTypeHandlerTest extends MessageTypeHandlerTestFixture {

    @Autowired
    MessageTypeHandler messageTypeHandler;

    @MockBean
    WebSocketChatSessions webSocketChatSessions;

    @Mock
    WebSocketSession writerSession;

    @Mock
    WebSocketSession receiverSession;

    @Autowired
    ApplicationEvents events;

    @Test
    void 웹소켓으로_메시지_전송시_사용자가_웹소켓에_접속하지_않은_경우_알림을_전송한다() throws JsonProcessingException {
        // given
        willReturn(false).given(webSocketChatSessions).containsByUserId(채팅방.getId(), 수신자.getId());
        willReturn(발신자만_존재하는_웹소켓_세션들).given(webSocketChatSessions).findSessionsByChatRoomId(채팅방.getId());

        // when
        messageTypeHandler.createResponse(발신자_세션_속성_dto, 메시지_데이터);
        final long actual = events.stream(MessageNotificationEvent.class).count();

        // then
        assertThat(actual).isEqualTo(1);
    }

    @Test
    void 웹소켓으로_메시지_전송시_메시지_수신자_모두의_메시지_로그_업데이트_이벤트를_호출한다() throws JsonProcessingException {
        // given
        willReturn(true).given(webSocketChatSessions).containsByUserId(채팅방.getId(), 발신자.getId());
        발신자와_수신자가_존재하는_웹소켓_세션들.putIfAbsent(writerSession, 채팅방.getId());
        발신자와_수신자가_존재하는_웹소켓_세션들.putIfAbsent(receiverSession, 채팅방.getId());
        willReturn(발신자와_수신자가_존재하는_웹소켓_세션들).given(webSocketChatSessions).findSessionsByChatRoomId(채팅방.getId());
        willReturn(발신자_세션_attribute_정보).given(writerSession).getAttributes();
        willReturn(수신자_세션_attribute_정보).given(receiverSession).getAttributes();

        // when
        messageTypeHandler.createResponse(발신자_세션_속성_dto, 메시지_데이터);
        final long actual = events.stream(UpdateReadMessageLogEvent.class).count();

        // then
        assertThat(actual).isEqualTo(2);
    }
}
