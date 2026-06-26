package com.knowave.monomarket.domains.chat.dto

import java.time.LocalDateTime
import java.util.UUID

data class ChatParticipantResponse(
    val id: UUID,
    val nickname: String,
)

data class SendChatMessageResponse(
    val chatMessageId: UUID,
    val chatRoomId: UUID,
    val sender: ChatParticipantResponse,
    val messageType: String,
    val content: String,
    val createdAt: LocalDateTime,
)
