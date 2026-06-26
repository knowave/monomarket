package com.knowave.monomarket.domains.chat.dto

import java.util.UUID

data class CreateOrGetChatRoomCommand(
    val requesterId: UUID,
    val productId: UUID,
)

data class GetManyChatRoomQuery(
    val userId: UUID,
    val page: Int,
    val size: Int,
)

data class SendChatMessageCommand(
    val senderId: UUID,
    val chatRoomId: UUID,
    val content: String,
)

data class GetManyChatMessageQuery(
    val requesterId: UUID,
    val chatRoomId: UUID,
    val page: Int,
    val size: Int,
)
