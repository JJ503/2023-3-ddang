package com.ddang.ddang.chat.application.event;

import com.ddang.ddang.chat.domain.Message;

public record MessageNotificationEvent(Message message, String profileImageAbsoluteUrl) {

    @Override
    public String toString() {
        return "MessageNotificationEvent{" + "message=" + message + ", profileImageAbsoluteUrl='" + profileImageAbsoluteUrl + '\'' + '}';
    }
}
