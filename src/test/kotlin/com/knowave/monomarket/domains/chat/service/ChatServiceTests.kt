package com.knowave.monomarket.domains.chat.service

import com.knowave.monomarket.common.exception.MonomarketException
import com.knowave.monomarket.domains.chat.dto.CreateOrGetChatRoomCommand
import com.knowave.monomarket.domains.chat.dto.GetManyChatMessageQuery
import com.knowave.monomarket.domains.chat.dto.GetManyChatRoomQuery
import com.knowave.monomarket.domains.chat.dto.SendChatMessageCommand
import com.knowave.monomarket.domains.chat.repository.ChatMessageRepository
import com.knowave.monomarket.domains.chat.repository.ChatRoomRepository
import com.knowave.monomarket.domains.product.entity.Product
import com.knowave.monomarket.domains.product.entity.ProductImage
import com.knowave.monomarket.domains.product.repository.ProductRepository
import com.knowave.monomarket.domains.user.entity.User
import com.knowave.monomarket.domains.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@SpringBootTest
@Transactional
class ChatServiceTests @Autowired constructor(
    private val chatService: ChatService,
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
) {
    @Test
    fun `buyer creates chat room for product`() {
        val seller = createUser("seller")
        val buyer = createUser("buyer")
        val product = createProduct(seller)

        val result = chatService.createOrGetChatRoom(
            CreateOrGetChatRoomCommand(
                requesterId = requireNotNull(buyer.id),
                productId = requireNotNull(product.id),
            )
        )

        assertNotNull(result.chatRoomId)
        assertEquals(product.id, result.productId)
        assertEquals(buyer.id, result.buyer.id)
        assertEquals(seller.id, result.seller.id)
        assertEquals(1, chatRoomRepository.count())
    }

    @Test
    fun `same product buyer seller combination returns existing chat room`() {
        val seller = createUser("seller")
        val buyer = createUser("buyer")
        val product = createProduct(seller)
        val command = CreateOrGetChatRoomCommand(
            requesterId = requireNotNull(buyer.id),
            productId = requireNotNull(product.id),
        )

        val firstResult = chatService.createOrGetChatRoom(command)
        val secondResult = chatService.createOrGetChatRoom(command)

        assertEquals(firstResult.chatRoomId, secondResult.chatRoomId)
        assertEquals(1, chatRoomRepository.count())
    }

    @Test
    fun `seller cannot create chat room for own product`() {
        val seller = createUser("seller")
        val product = createProduct(seller)

        val exception = assertThrows<MonomarketException> {
            chatService.createOrGetChatRoom(
                CreateOrGetChatRoomCommand(
                    requesterId = requireNotNull(seller.id),
                    productId = requireNotNull(product.id),
                )
            )
        }

        assertEquals("CHAT_ROOM_SELF_FORBIDDEN", exception.errorCode)
    }

    @Test
    fun `participant sends chat message and updates room last message`() {
        val seller = createUser("seller")
        val buyer = createUser("buyer")
        val product = createProduct(seller)
        val chatRoom = createChatRoom(buyer, product)

        val result = chatService.sendChatMessage(
            SendChatMessageCommand(
                senderId = requireNotNull(buyer.id),
                chatRoomId = chatRoom.chatRoomId,
                content = " hello ",
            )
        )
        val savedChatRoom = chatRoomRepository.findChatRoomById(chatRoom.chatRoomId)

        assertEquals("hello", result.content)
        assertEquals("TEXT", result.messageType)
        assertEquals("hello", savedChatRoom?.lastMessage)
        assertNotNull(savedChatRoom?.lastMessageAt)
        assertEquals(1, chatMessageRepository.count())
    }

    @Test
    fun `non participant cannot send or read chat messages`() {
        val seller = createUser("seller")
        val buyer = createUser("buyer")
        val stranger = createUser("stranger")
        val product = createProduct(seller)
        val chatRoom = createChatRoom(buyer, product)

        val sendException = assertThrows<MonomarketException> {
            chatService.sendChatMessage(
                SendChatMessageCommand(
                    senderId = requireNotNull(stranger.id),
                    chatRoomId = chatRoom.chatRoomId,
                    content = "hello",
                )
            )
        }
        val readException = assertThrows<MonomarketException> {
            chatService.getManyChatMessageByQuery(
                GetManyChatMessageQuery(
                    requesterId = requireNotNull(stranger.id),
                    chatRoomId = chatRoom.chatRoomId,
                    page = 0,
                    size = 20,
                )
            )
        }

        assertEquals("CHAT_ROOM_PARTICIPANT_FORBIDDEN", sendException.errorCode)
        assertEquals("CHAT_ROOM_PARTICIPANT_FORBIDDEN", readException.errorCode)
    }

    @Test
    fun `blank message content is rejected`() {
        val seller = createUser("seller")
        val buyer = createUser("buyer")
        val product = createProduct(seller)
        val chatRoom = createChatRoom(buyer, product)

        val exception = assertThrows<MonomarketException> {
            chatService.sendChatMessage(
                SendChatMessageCommand(
                    senderId = requireNotNull(buyer.id),
                    chatRoomId = chatRoom.chatRoomId,
                    content = "   ",
                )
            )
        }

        assertEquals("INVALID_CHAT_MESSAGE_CONTENT", exception.errorCode)
    }

    @Test
    fun `chat rooms are listed by participant and latest activity first`() {
        val seller = createUser("seller")
        val buyer = createUser("buyer")
        val anotherBuyer = createUser("another")
        val firstProduct = createProduct(seller, title = "first", thumbnailObjectKey = "products/first-thumbnail.png")
        val secondProduct = createProduct(seller, title = "second")
        val excludedProduct = createProduct(seller, title = "excluded")
        val firstRoom = createChatRoom(buyer, firstProduct)
        val secondRoom = createChatRoom(buyer, secondProduct)
        createChatRoom(anotherBuyer, excludedProduct)

        chatService.sendChatMessage(
            SendChatMessageCommand(
                senderId = requireNotNull(buyer.id),
                chatRoomId = firstRoom.chatRoomId,
                content = "first message",
            )
        )
        chatService.sendChatMessage(
            SendChatMessageCommand(
                senderId = requireNotNull(buyer.id),
                chatRoomId = secondRoom.chatRoomId,
                content = "second message",
            )
        )

        val result = chatService.getManyChatRoomByQuery(
            GetManyChatRoomQuery(
                userId = requireNotNull(buyer.id),
                page = 0,
                size = 20,
            )
        )

        assertEquals(2, result.totalElements)
        assertEquals(listOf(secondRoom.chatRoomId, firstRoom.chatRoomId), result.content.map { it.chatRoomId })
        assertTrue(result.content.all { it.buyer.id == buyer.id || it.seller.id == buyer.id })
        assertEquals(
            "https://cdn.example.com/products/first-thumbnail.png",
            result.content.single { it.chatRoomId == firstRoom.chatRoomId }.productThumbnailUrl,
        )
        assertEquals(null, result.content.single { it.chatRoomId == secondRoom.chatRoomId }.productThumbnailUrl)
    }

    @Test
    fun `chat messages are listed by created at descending`() {
        val seller = createUser("seller")
        val buyer = createUser("buyer")
        val product = createProduct(seller)
        val chatRoom = createChatRoom(buyer, product)
        val firstMessage = chatService.sendChatMessage(
            SendChatMessageCommand(
                senderId = requireNotNull(buyer.id),
                chatRoomId = chatRoom.chatRoomId,
                content = "first",
            )
        )
        val secondMessage = chatService.sendChatMessage(
            SendChatMessageCommand(
                senderId = requireNotNull(seller.id),
                chatRoomId = chatRoom.chatRoomId,
                content = "second",
            )
        )

        val result = chatService.getManyChatMessageByQuery(
            GetManyChatMessageQuery(
                requesterId = requireNotNull(buyer.id),
                chatRoomId = chatRoom.chatRoomId,
                page = 0,
                size = 20,
            )
        )

        assertEquals(2, result.totalElements)
        assertEquals(listOf(secondMessage.chatMessageId, firstMessage.chatMessageId), result.content.map { it.chatMessageId })
    }

    private fun createChatRoom(
        buyer: User,
        product: Product,
    ) = chatService.createOrGetChatRoom(
        CreateOrGetChatRoomCommand(
            requesterId = requireNotNull(buyer.id),
            productId = requireNotNull(product.id),
        )
    )

    private fun createUser(prefix: String): User {
        return userRepository.save(
            User(nickname = "${prefix}_${UUID.randomUUID().toString().take(8)}")
        )
    }

    private fun createProduct(
        seller: User,
        title: String = "product",
        thumbnailObjectKey: String? = null,
    ): Product {
        val product = productRepository.save(
            Product(
                seller = seller,
                title = "${title}_${UUID.randomUUID().toString().take(8)}",
                description = "description",
                price = 10_000,
            )
        )
        if (thumbnailObjectKey != null) {
            product.replaceImages(
                listOf(ProductImage(product = product, objectKey = thumbnailObjectKey, sortOrder = 0))
            )
            productRepository.save(product)
        }

        return product
    }
}
