package com.knowave.monomarket.domains.chat.entity

import com.knowave.monomarket.common.entity.BaseEntity
import com.knowave.monomarket.domains.product.entity.Product
import com.knowave.monomarket.domains.user.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    name = "chat_rooms",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_chat_room_product_buyer_seller",
            columnNames = ["product_id", "buyer_id", "seller_id"],
        ),
    ],
    indexes = [
        Index(
            name = "idx_chat_room_buyer_last_message",
            columnList = "buyer_id, last_message_at",
        ),
        Index(
            name = "idx_chat_room_seller_last_message",
            columnList = "seller_id, last_message_at",
        ),
    ],
)
class ChatRoom(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    val buyer: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    val seller: User,

    @Column(name = "last_message", columnDefinition = "TEXT")
    var lastMessage: String? = null,

    @Column(name = "last_message_at")
    var lastMessageAt: LocalDateTime? = null,
) : BaseEntity() {
    fun updateLastMessage(
        content: String,
        sentAt: LocalDateTime,
    ) {
        lastMessage = content
        lastMessageAt = sentAt
    }
}
