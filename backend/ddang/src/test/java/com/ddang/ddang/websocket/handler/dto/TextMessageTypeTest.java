package com.ddang.ddang.websocket.handler.dto;

import com.ddang.ddang.websocket.handler.exception.UnsupportedTextMessageTypeException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class TextMessageTypeTest {

    @Test
    void 메시지_타입에_해당하는_문자열을_enum으로_반환한다() {
        // given
        final String type = "chattings";

        // when
        final TextMessageType actual = TextMessageType.fromString(type);

        // then
        assertThat(actual).isEqualTo(TextMessageType.CHATTINGS);
    }

    @Test
    void 메시지_타입_입력값은_대소문자를_구분하지_않는다() {
        // given
        final String type = "CHATTINGS";

        // when
        final TextMessageType actual = TextMessageType.fromString(type);

        // then
        assertThat(actual).isEqualTo(TextMessageType.CHATTINGS);
    }

    @Test
    void 잘못된_타입이_전송되면_예외를_반환한다() {
        // given
        final String wrongType = "wrong type";

        // when & then
        assertThatThrownBy(() -> TextMessageType.fromString(wrongType))
                .isInstanceOf(UnsupportedTextMessageTypeException.class);
    }
}
