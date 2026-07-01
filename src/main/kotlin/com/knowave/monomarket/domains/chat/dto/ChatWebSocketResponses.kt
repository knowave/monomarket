package com.knowave.monomarket.domains.chat.dto

import java.time.LocalDateTime
import java.util.UUID

data class ChatMessageResponse(
    val messageId: UUID,
    val chatRoomId: UUID,
    val senderId: UUID,
    val messageType: String,
    val content: String,
    val createdAt: LocalDateTime,
)
