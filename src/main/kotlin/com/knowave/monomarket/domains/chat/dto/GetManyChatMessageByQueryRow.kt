package com.knowave.monomarket.domains.chat.dto

import java.time.LocalDateTime

interface GetManyChatMessageByQueryRow {
    val chatMessageId: String
    val chatRoomId: String
    val senderId: String
    val senderNickname: String
    val messageType: String
    val content: String
    val createdAt: LocalDateTime
    val totalElements: Long
}
