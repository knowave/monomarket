package com.knowave.monomarket.domains.chat.dto

import java.time.LocalDateTime

interface GetManyChatRoomByQueryRow {
    val chatRoomId: String
    val productId: String
    val productTitle: String
    val productPrice: Long
    val productStatus: String
    val buyerId: String
    val buyerNickname: String
    val sellerId: String
    val sellerNickname: String
    val lastMessage: String?
    val lastMessageAt: LocalDateTime?
    val createdAt: LocalDateTime
    val totalElements: Long
}
