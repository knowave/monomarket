package com.knowave.monomarket.domains.chat.entity

import com.knowave.monomarket.common.entity.BaseEntity
import com.knowave.monomarket.domains.user.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(
    name = "chat_messages",
    indexes = [
        Index(
            name = "idx_chat_message_room_created",
            columnList = "chat_room_id, created_at",
        ),
    ],
)
class ChatMessage(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    val chatRoom: ChatRoom,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    val sender: User,

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 30)
    val messageType: MessageType = MessageType.TEXT,

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,
) : BaseEntity()
