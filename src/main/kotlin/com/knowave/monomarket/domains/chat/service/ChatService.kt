package com.knowave.monomarket.domains.chat.service

import com.knowave.monomarket.domains.chat.dto.ChatMessageResult
import com.knowave.monomarket.domains.chat.dto.ChatParticipantResult
import com.knowave.monomarket.domains.chat.dto.ChatRoomSummaryResult
import com.knowave.monomarket.domains.chat.dto.CreateOrGetChatRoomCommand
import com.knowave.monomarket.domains.chat.dto.CreateOrGetChatRoomResult
import com.knowave.monomarket.domains.chat.dto.GetManyChatMessageByQueryRow
import com.knowave.monomarket.domains.chat.dto.GetManyChatMessageQuery
import com.knowave.monomarket.domains.chat.dto.GetManyChatMessageResult
import com.knowave.monomarket.domains.chat.dto.GetManyChatRoomByQueryRow
import com.knowave.monomarket.domains.chat.dto.GetManyChatRoomQuery
import com.knowave.monomarket.domains.chat.dto.GetManyChatRoomResult
import com.knowave.monomarket.domains.chat.dto.SendChatMessageCommand
import com.knowave.monomarket.domains.chat.dto.SendChatMessageResult
import com.knowave.monomarket.domains.chat.entity.ChatMessage
import com.knowave.monomarket.domains.chat.entity.ChatRoom
import com.knowave.monomarket.domains.chat.entity.MessageType
import com.knowave.monomarket.domains.chat.exception.ChatExceptions
import com.knowave.monomarket.domains.chat.repository.ChatMessageRepository
import com.knowave.monomarket.domains.chat.repository.ChatRoomRepository
import com.knowave.monomarket.domains.product.service.ProductService
import com.knowave.monomarket.domains.user.entity.User
import com.knowave.monomarket.domains.user.service.UserService
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class ChatService(
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val userService: UserService,
    private val productService: ProductService,
) {
    private val maxPageSize = 100

    @Transactional
    fun createOrGetChatRoom(command: CreateOrGetChatRoomCommand): CreateOrGetChatRoomResult {
        val buyer = userService.getUser(command.requesterId)
        val product = productService.getProductForChatRoom(command.productId)
        val seller = product.seller
        val buyerId = requireNotNull(buyer.id)
        val sellerId = requireNotNull(seller.id)

        if (buyerId == sellerId) {
            throw ChatExceptions.selfChatRoomForbidden()
        }

        val chatRoom = chatRoomRepository.findChatRoomByProductIdAndBuyerIdAndSellerId(
            productId = command.productId,
            buyerId = buyerId,
            sellerId = sellerId,
        ) ?: saveChatRoom(
            chatRoom = ChatRoom(
                product = product,
                buyer = buyer,
                seller = seller,
            ),
            productId = command.productId,
            buyerId = buyerId,
            sellerId = sellerId,
        )

        return toCreateOrGetChatRoomResult(chatRoom)
    }

    @Transactional(readOnly = true)
    fun getManyChatRoomByQuery(command: GetManyChatRoomQuery): GetManyChatRoomResult {
        userService.getUser(command.userId)

        val pageable = PageRequest.of(
            command.page,
            command.size.coerceAtMost(maxPageSize),
        )
        val chatRooms = chatRoomRepository.findManyChatRoomByQuery(
            userId = command.userId,
            limit = pageable.pageSize,
            offset = pageable.offset,
        )
        val totalElements = chatRooms.firstOrNull()?.totalElements ?: 0

        return GetManyChatRoomResult(
            content = chatRooms.map { row -> toChatRoomSummaryResult(row) },
            page = pageable.pageNumber,
            size = pageable.pageSize,
            totalElements = totalElements,
            totalPages = calculateTotalPages(totalElements, pageable.pageSize),
            hasNext = pageable.offset + chatRooms.size < totalElements,
        )
    }

    @Transactional
    fun sendChatMessage(command: SendChatMessageCommand): SendChatMessageResult {
        val chatRoom = getChatRoom(command.chatRoomId)
        val sender = getParticipant(
            chatRoom = chatRoom,
            userId = command.senderId,
        )
        val content = command.content.trim()

        if (content.isBlank()) {
            throw ChatExceptions.invalidMessageContent()
        }

        val sentAt = LocalDateTime.now()
        val chatMessage = chatMessageRepository.save(
            ChatMessage(
                chatRoom = chatRoom,
                sender = sender,
                messageType = MessageType.TEXT,
                content = content,
            )
        )
        chatRoom.updateLastMessage(
            content = content,
            sentAt = chatMessage.createdAt ?: sentAt,
        )

        return SendChatMessageResult(
            chatMessageId = requireNotNull(chatMessage.id),
            chatRoomId = requireNotNull(chatRoom.id),
            sender = toChatParticipantResult(sender),
            messageType = chatMessage.messageType.name,
            content = chatMessage.content,
            createdAt = chatMessage.createdAt ?: sentAt,
        )
    }

    @Transactional(readOnly = true)
    fun getManyChatMessageByQuery(command: GetManyChatMessageQuery): GetManyChatMessageResult {
        val chatRoom = getChatRoom(command.chatRoomId)
        validateParticipant(
            chatRoom = chatRoom,
            userId = command.requesterId,
        )

        val pageable = PageRequest.of(
            command.page,
            command.size.coerceAtMost(maxPageSize),
        )
        val chatMessages = chatMessageRepository.findManyChatMessageByQuery(
            chatRoomId = command.chatRoomId,
            limit = pageable.pageSize,
            offset = pageable.offset,
        )
        val totalElements = chatMessages.firstOrNull()?.totalElements ?: 0

        return GetManyChatMessageResult(
            content = chatMessages.map { row -> toChatMessageResult(row) },
            page = pageable.pageNumber,
            size = pageable.pageSize,
            totalElements = totalElements,
            totalPages = calculateTotalPages(totalElements, pageable.pageSize),
            hasNext = pageable.offset + chatMessages.size < totalElements,
        )
    }

    private fun saveChatRoom(
        chatRoom: ChatRoom,
        productId: UUID,
        buyerId: UUID,
        sellerId: UUID,
    ): ChatRoom {
        return try {
            chatRoomRepository.saveAndFlush(chatRoom)
        } catch (exception: DataIntegrityViolationException) {
            chatRoomRepository.findChatRoomByProductIdAndBuyerIdAndSellerId(
                productId = productId,
                buyerId = buyerId,
                sellerId = sellerId,
            ) ?: throw exception
        }
    }

    private fun getChatRoom(chatRoomId: UUID): ChatRoom {
        return chatRoomRepository.findChatRoomById(chatRoomId)
            ?: throw ChatExceptions.chatRoomNotFound()
    }

    private fun getParticipant(
        chatRoom: ChatRoom,
        userId: UUID,
    ): User {
        val buyerId = requireNotNull(chatRoom.buyer.id)
        val sellerId = requireNotNull(chatRoom.seller.id)

        return when (userId) {
            buyerId -> chatRoom.buyer
            sellerId -> chatRoom.seller
            else -> throw ChatExceptions.chatRoomParticipantForbidden()
        }
    }

    private fun validateParticipant(
        chatRoom: ChatRoom,
        userId: UUID,
    ) {
        getParticipant(
            chatRoom = chatRoom,
            userId = userId,
        )
    }

    private fun toCreateOrGetChatRoomResult(chatRoom: ChatRoom): CreateOrGetChatRoomResult {
        return CreateOrGetChatRoomResult(
            chatRoomId = requireNotNull(chatRoom.id),
            productId = requireNotNull(chatRoom.product.id),
            productTitle = chatRoom.product.title,
            buyer = toChatParticipantResult(chatRoom.buyer),
            seller = toChatParticipantResult(chatRoom.seller),
            lastMessage = chatRoom.lastMessage,
            lastMessageAt = chatRoom.lastMessageAt,
            createdAt = requireNotNull(chatRoom.createdAt),
        )
    }

    private fun toChatRoomSummaryResult(row: GetManyChatRoomByQueryRow): ChatRoomSummaryResult {
        return ChatRoomSummaryResult(
            chatRoomId = UUID.fromString(row.chatRoomId),
            productId = UUID.fromString(row.productId),
            productTitle = row.productTitle,
            productPrice = row.productPrice,
            productStatus = row.productStatus,
            buyer = ChatParticipantResult(
                id = UUID.fromString(row.buyerId),
                nickname = row.buyerNickname,
            ),
            seller = ChatParticipantResult(
                id = UUID.fromString(row.sellerId),
                nickname = row.sellerNickname,
            ),
            lastMessage = row.lastMessage,
            lastMessageAt = row.lastMessageAt,
            createdAt = row.createdAt,
        )
    }

    private fun toChatMessageResult(row: GetManyChatMessageByQueryRow): ChatMessageResult {
        return ChatMessageResult(
            chatMessageId = UUID.fromString(row.chatMessageId),
            chatRoomId = UUID.fromString(row.chatRoomId),
            sender = ChatParticipantResult(
                id = UUID.fromString(row.senderId),
                nickname = row.senderNickname,
            ),
            messageType = row.messageType,
            content = row.content,
            createdAt = row.createdAt,
        )
    }

    private fun toChatParticipantResult(user: User): ChatParticipantResult {
        return ChatParticipantResult(
            id = requireNotNull(user.id),
            nickname = user.nickname,
        )
    }

    private fun calculateTotalPages(
        totalElements: Long,
        size: Int,
    ): Int {
        if (totalElements == 0L) {
            return 0
        }

        return ((totalElements + size - 1) / size).toInt()
    }
}
