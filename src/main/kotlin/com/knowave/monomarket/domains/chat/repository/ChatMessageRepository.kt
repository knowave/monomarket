package com.knowave.monomarket.domains.chat.repository

import com.knowave.monomarket.domains.chat.dto.GetManyChatMessageByQueryRow
import com.knowave.monomarket.domains.chat.entity.ChatMessage
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface ChatMessageRepository : JpaRepository<ChatMessage, UUID> {
    @EntityGraph(attributePaths = ["chatRoom", "sender"])
    fun findChatMessageById(id: UUID): ChatMessage?

    @Query(
        value = """
            select
                cast(chat_message.id as varchar) as "chatMessageId",
                cast(chat_message.chat_room_id as varchar) as "chatRoomId",
                cast(sender.id as varchar) as "senderId",
                sender.nickname as "senderNickname",
                chat_message.message_type as "messageType",
                chat_message.content as "content",
                chat_message.created_at as "createdAt",
                count(*) over() as "totalElements"
            from chat_messages chat_message
            join users sender on sender.id = chat_message.sender_id
            where chat_message.chat_room_id = :chatRoomId
            order by chat_message.created_at desc,
                     chat_message.id desc
            limit :limit offset :offset
        """,
        nativeQuery = true,
    )
    fun findManyChatMessageByQuery(
        chatRoomId: UUID,
        limit: Int,
        offset: Long,
    ): List<GetManyChatMessageByQueryRow>
}
