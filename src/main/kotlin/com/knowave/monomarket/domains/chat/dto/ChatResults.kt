package com.knowave.monomarket.domains.chat.dto

import java.time.LocalDateTime
import java.util.UUID

data class ChatParticipantResult(
    val id: UUID,
    val nickname: String,
)

data class CreateOrGetChatRoomResult(
    val chatRoomId: UUID,
    val productId: UUID,
    val productTitle: String,
    val buyer: ChatParticipantResult,
    val seller: ChatParticipantResult,
    val lastMessage: String?,
    val lastMessageAt: LocalDateTime?,
    val createdAt: LocalDateTime,
)

data class ChatRoomSummaryResult(
    val chatRoomId: UUID,
    val productId: UUID,
    val productTitle: String,
    val productPrice: Long,
    val productStatus: String,
    val productThumbnailUrl: String?,
    val buyer: ChatParticipantResult,
    val seller: ChatParticipantResult,
    val lastMessage: String?,
    val lastMessageAt: LocalDateTime?,
    val createdAt: LocalDateTime,
)

data class GetManyChatRoomResult(
    val content: List<ChatRoomSummaryResult>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class SendChatMessageResult(
    val chatMessageId: UUID,
    val chatRoomId: UUID,
    val sender: ChatParticipantResult,
    val messageType: String,
    val content: String,
    val createdAt: LocalDateTime,
)

data class ChatMessageResult(
    val chatMessageId: UUID,
    val chatRoomId: UUID,
    val sender: ChatParticipantResult,
    val messageType: String,
    val content: String,
    val createdAt: LocalDateTime,
)

data class GetManyChatMessageResult(
    val content: List<ChatMessageResult>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)
