package com.knowave.monomarket.domains.chat.repository

import com.knowave.monomarket.domains.chat.dto.GetManyChatRoomByQueryRow
import com.knowave.monomarket.domains.chat.entity.ChatRoom
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface ChatRoomRepository : JpaRepository<ChatRoom, UUID> {
    @EntityGraph(attributePaths = ["product", "buyer", "seller"])
    fun findChatRoomById(id: UUID): ChatRoom?

    @EntityGraph(attributePaths = ["product", "buyer", "seller"])
    fun findChatRoomByProductIdAndBuyerIdAndSellerId(
        productId: UUID,
        buyerId: UUID,
        sellerId: UUID,
    ): ChatRoom?

    @Query(
        value = """
            select
                cast(chat_room.id as varchar) as "chatRoomId",
                cast(product.id as varchar) as "productId",
                product.title as "productTitle",
                product.price as "productPrice",
                product.status as "productStatus",
                cast(buyer.id as varchar) as "buyerId",
                buyer.nickname as "buyerNickname",
                cast(seller.id as varchar) as "sellerId",
                seller.nickname as "sellerNickname",
                chat_room.last_message as "lastMessage",
                chat_room.last_message_at as "lastMessageAt",
                chat_room.created_at as "createdAt",
                count(*) over() as "totalElements"
            from chat_rooms chat_room
            join products product on product.id = chat_room.product_id
            join users buyer on buyer.id = chat_room.buyer_id
            join users seller on seller.id = chat_room.seller_id
            where chat_room.buyer_id = :userId
               or chat_room.seller_id = :userId
            order by coalesce(chat_room.last_message_at, chat_room.created_at) desc,
                     chat_room.created_at desc
            limit :limit offset :offset
        """,
        nativeQuery = true,
    )
    fun findManyChatRoomByQuery(
        userId: UUID,
        limit: Int,
        offset: Long,
    ): List<GetManyChatRoomByQueryRow>
}
