package com.knowave.monomarket.domains.chat.controller

import com.knowave.monomarket.common.config.S3Properties
import com.knowave.monomarket.domains.auth.principal.CustomUserPrincipal
import com.knowave.monomarket.domains.chat.dto.ChatMessageResult
import com.knowave.monomarket.domains.chat.dto.ChatParticipantResult
import com.knowave.monomarket.domains.chat.dto.ChatRoomSummaryResult
import com.knowave.monomarket.domains.chat.dto.CreateOrGetChatRoomCommand
import com.knowave.monomarket.domains.chat.dto.CreateOrGetChatRoomRequest
import com.knowave.monomarket.domains.chat.dto.CreateOrGetChatRoomResult
import com.knowave.monomarket.domains.chat.dto.GetManyChatMessageQuery
import com.knowave.monomarket.domains.chat.dto.GetManyChatMessageRequest
import com.knowave.monomarket.domains.chat.dto.GetManyChatMessageResult
import com.knowave.monomarket.domains.chat.dto.GetManyChatRoomQuery
import com.knowave.monomarket.domains.chat.dto.GetManyChatRoomRequest
import com.knowave.monomarket.domains.chat.dto.GetManyChatRoomResult
import com.knowave.monomarket.domains.chat.repository.ChatMessageRepository
import com.knowave.monomarket.domains.chat.repository.ChatRoomRepository
import com.knowave.monomarket.domains.chat.service.ChatService
import com.knowave.monomarket.domains.product.service.ProductService
import com.knowave.monomarket.domains.user.service.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.UUID

class ChatControllerTests {
    private val buyerId = UUID.randomUUID()
    private val sellerId = UUID.randomUUID()
    private val chatRoomId = UUID.randomUUID()
    private val productId = UUID.randomUUID()
    private val createdAt = LocalDateTime.now()

    @Test
    fun `create or get chat room converts principal and request to command then returns response`() {
        lateinit var capturedCommand: CreateOrGetChatRoomCommand
        val result = CreateOrGetChatRoomResult(
            chatRoomId = chatRoomId,
            productId = productId,
            productTitle = "product",
            buyer = ChatParticipantResult(id = buyerId, nickname = "buyer"),
            seller = ChatParticipantResult(id = sellerId, nickname = "seller"),
            lastMessage = null,
            lastMessageAt = null,
            createdAt = createdAt,
        )
        val controller = controllerWith(
            createOrGetChatRoom = { command ->
                capturedCommand = command
                result
            },
        )

        val response = controller.createOrGetChatRoom(
            principal = CustomUserPrincipal(buyerId),
            request = CreateOrGetChatRoomRequest(productId = productId),
        )

        assertEquals(buyerId, capturedCommand.requesterId)
        assertEquals(productId, capturedCommand.productId)
        assertEquals(chatRoomId, response.chatRoomId)
        assertEquals(buyerId, response.buyerId)
        assertEquals(sellerId, response.sellerId)
        assertEquals(createdAt, response.createdAt)
    }

    @Test
    fun `get many chat room resolves the opponent relative to the requester`() {
        val result = GetManyChatRoomResult(
            content = listOf(
                ChatRoomSummaryResult(
                    chatRoomId = chatRoomId,
                    productId = productId,
                    productTitle = "product",
                    productPrice = 10_000,
                    productStatus = "ON_SALE",
                    productThumbnailUrl = "https://cdn.example.com/thumbnail.png",
                    buyer = ChatParticipantResult(id = buyerId, nickname = "buyer"),
                    seller = ChatParticipantResult(id = sellerId, nickname = "seller"),
                    lastMessage = "hello",
                    lastMessageAt = createdAt,
                    createdAt = createdAt,
                ),
            ),
            page = 0,
            size = 20,
            totalElements = 1,
            totalPages = 1,
            hasNext = false,
        )
        lateinit var capturedQuery: GetManyChatRoomQuery
        val controller = controllerWith(
            getManyChatRoomByQuery = { query ->
                capturedQuery = query
                result
            },
        )

        val response = controller.getManyChatRoomByQuery(
            principal = CustomUserPrincipal(buyerId),
            request = GetManyChatRoomRequest(page = 0, size = 20),
        )

        assertEquals(buyerId, capturedQuery.userId)
        val summary = response.content.single()
        assertEquals(sellerId, summary.opponentUserId)
        assertEquals("seller", summary.opponentNickname)
        assertEquals("https://cdn.example.com/thumbnail.png", summary.productThumbnailUrl)
    }

    @Test
    fun `get many chat message converts path variable and query then returns response`() {
        val result = GetManyChatMessageResult(
            content = listOf(
                ChatMessageResult(
                    chatMessageId = UUID.randomUUID(),
                    chatRoomId = chatRoomId,
                    sender = ChatParticipantResult(id = buyerId, nickname = "buyer"),
                    messageType = "TEXT",
                    content = "hello",
                    createdAt = createdAt,
                ),
            ),
            page = 0,
            size = 30,
            totalElements = 1,
            totalPages = 1,
            hasNext = false,
        )
        lateinit var capturedQuery: GetManyChatMessageQuery
        val controller = controllerWith(
            getManyChatMessageByQuery = { query ->
                capturedQuery = query
                result
            },
        )

        val response = controller.getManyChatMessageByQuery(
            principal = CustomUserPrincipal(buyerId),
            chatRoomId = chatRoomId,
            request = GetManyChatMessageRequest(page = 0, size = 30),
        )

        assertEquals(buyerId, capturedQuery.requesterId)
        assertEquals(chatRoomId, capturedQuery.chatRoomId)
        val message = response.content.single()
        assertEquals(buyerId, message.senderId)
        assertEquals("buyer", message.senderNickname)
    }

    private fun controllerWith(
        createOrGetChatRoom: (CreateOrGetChatRoomCommand) -> CreateOrGetChatRoomResult = { error("not stubbed") },
        getManyChatRoomByQuery: (GetManyChatRoomQuery) -> GetManyChatRoomResult = { error("not stubbed") },
        getManyChatMessageByQuery: (GetManyChatMessageQuery) -> GetManyChatMessageResult = { error("not stubbed") },
    ): ChatController {
        val chatService = object : ChatService(
            chatRoomRepository = mock(ChatRoomRepository::class.java),
            chatMessageRepository = mock(ChatMessageRepository::class.java),
            userService = mock(UserService::class.java),
            productService = mock(ProductService::class.java),
            s3Properties = mock(S3Properties::class.java),
        ) {
            override fun createOrGetChatRoom(command: CreateOrGetChatRoomCommand) = createOrGetChatRoom(command)

            override fun getManyChatRoomByQuery(command: GetManyChatRoomQuery) = getManyChatRoomByQuery(command)

            override fun getManyChatMessageByQuery(command: GetManyChatMessageQuery) = getManyChatMessageByQuery(command)
        }

        return ChatController(chatService)
    }
}
