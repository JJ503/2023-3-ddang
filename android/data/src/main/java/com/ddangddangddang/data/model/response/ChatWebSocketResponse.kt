package com.ddangddangddang.data.model.response

data class ChatWebSocketResponse(
    val status: String,
    val messages: List<ChatMessageResponse>,
)
