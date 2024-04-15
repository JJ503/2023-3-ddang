package com.ddangddangddang.data.model.response

data class ChatWebSocketResponse(
    val sendMessageStatus: String,
    val messages: List<ChatMessageResponse>,
)
