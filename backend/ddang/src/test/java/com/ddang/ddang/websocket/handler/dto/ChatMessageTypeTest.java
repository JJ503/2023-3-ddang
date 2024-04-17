package com.ddang.ddang.websocket.handler.dto;

import com.ddang.ddang.websocket.handler.exception.UnsupportedChattingTypeException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
class ChatMessageTypeTest {

    @Test
    void 타입에_해당하는_enum을_반환한다() {
        // given
        final Map<String, String> data = Map.of("type", "message");

        // when
        final ChatMessageType actual = ChatMessageType.findMessageType(data.get("type"));

        // then
        assertThat(actual).isEqualTo(ChatMessageType.MESSAGE);
    }


    @Test
    void 해당하는_타입이_없는_경우_예외를_던진다() {
        // given
        final Map<String, String> data = Map.of("type", "wrong type");

        // when & then
        assertThatThrownBy(() -> ChatMessageType.findMessageType(data.get("type")))
                .isInstanceOf(UnsupportedChattingTypeException.class);
    }
}
