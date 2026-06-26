package com.knowave.monomarket.domains.chat.controller

import com.knowave.monomarket.domains.auth.principal.CustomUserPrincipal
import com.knowave.monomarket.domains.chat.dto.ChatParticipantResult
import com.knowave.monomarket.domains.chat.dto.SendChatMessageCommand
import com.knowave.monomarket.domains.chat.dto.SendChatMessageRequest
import com.knowave.monomarket.domains.chat.dto.SendChatMessageResult
import com.knowave.monomarket.domains.chat.repository.ChatMessageRepository
import com.knowave.monomarket.domains.chat.repository.ChatRoomRepository
import com.knowave.monomarket.domains.chat.service.ChatService
import com.knowave.monomarket.domains.product.service.ProductService
import com.knowave.monomarket.domains.user.service.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import java.time.LocalDateTime
import java.util.UUID

class ChatWebSocketControllerTests {
    @Test
    fun `send chat message converts principal and request to command then returns response`() {
        val senderId = UUID.randomUUID()
        val chatRoomId = UUID.randomUUID()
        val chatMessageId = UUID.randomUUID()
        val createdAt = LocalDateTime.now()
        lateinit var capturedCommand: SendChatMessageCommand
        val result = SendChatMessageResult(
            chatMessageId = chatMessageId,
            chatRoomId = chatRoomId,
            sender = ChatParticipantResult(
                id = senderId,
                nickname = "buyer",
            ),
            messageType = "TEXT",
            content = "hello",
            createdAt = createdAt,
        )
        val chatService = object : ChatService(
            chatRoomRepository = mock(ChatRoomRepository::class.java),
            chatMessageRepository = mock(ChatMessageRepository::class.java),
            userService = mock(UserService::class.java),
            productService = mock(ProductService::class.java),
        ) {
            override fun sendChatMessage(command: SendChatMessageCommand): SendChatMessageResult {
                capturedCommand = command
                return result
            }
        }
        val controller = ChatWebSocketController(chatService)

        val response = controller.sendChatMessage(
            principal = UsernamePasswordAuthenticationToken(CustomUserPrincipal(senderId), "token", emptyList()),
            request = SendChatMessageRequest(
                chatRoomId = chatRoomId,
                content = "hello",
            ),
        )

        assertEquals(senderId, capturedCommand.senderId)
        assertEquals(chatRoomId, capturedCommand.chatRoomId)
        assertEquals("hello", capturedCommand.content)
        assertEquals(chatMessageId, response.chatMessageId)
        assertEquals(senderId, response.sender.id)
        assertEquals("TEXT", response.messageType)
        assertEquals(createdAt, response.createdAt)
    }
}
