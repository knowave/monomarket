package com.knowave.monomarket.domains.chat.dto

import java.util.UUID

data class SendChatMessageRequest(
    val chatRoomId: UUID,
    val content: String,
)
