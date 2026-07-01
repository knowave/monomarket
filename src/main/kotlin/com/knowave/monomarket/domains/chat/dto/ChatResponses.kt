package com.knowave.monomarket.domains.chat.dto

import java.time.LocalDateTime
import java.util.UUID

data class CreateOrGetChatRoomResponse(
    val chatRoomId: UUID,
    val productId: UUID,
    val buyerId: UUID,
    val sellerId: UUID,
    val lastMessage: String?,
    val lastMessageAt: LocalDateTime?,
    val createdAt: LocalDateTime,
)

data class ChatRoomSummaryResponse(
    val chatRoomId: UUID,
    val productId: UUID,
    val productTitle: String,
    val productThumbnailUrl: String?,
    val opponentUserId: UUID,
    val opponentNickname: String,
    val lastMessage: String?,
    val lastMessageAt: LocalDateTime?,
    val createdAt: LocalDateTime,
)

data class GetManyChatRoomResponse(
    val content: List<ChatRoomSummaryResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class GetManyChatMessageResponse(
    val content: List<ChatMessageResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)
