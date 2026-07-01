package com.knowave.monomarket.domains.chat.controller

import com.knowave.monomarket.domains.auth.principal.CustomUserPrincipal
import com.knowave.monomarket.domains.chat.dto.ChatMessageResponse
import com.knowave.monomarket.domains.chat.dto.SendChatMessageCommand
import com.knowave.monomarket.domains.chat.dto.SendChatMessageRequest
import com.knowave.monomarket.domains.chat.dto.SendChatMessageResult
import com.knowave.monomarket.domains.chat.service.ChatService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import java.security.Principal
import java.util.UUID

@Controller
class ChatWebSocketController(
    private val chatService: ChatService,
    private val messagingTemplate: SimpMessagingTemplate,
) {
    private val log = LoggerFactory.getLogger(ChatWebSocketController::class.java)

    @MessageMapping("/chat.send")
    fun sendChatMessage(
        principal: Principal,
        @Valid @Payload request: SendChatMessageRequest,
    ) {
        try {
            val userId = principal.extractUserPrincipal().userId
            val response = chatService.sendChatMessage(request.toCommand(userId)).toResponse()
            messagingTemplate.convertAndSend(
                "/topic/chat-rooms/${response.chatRoomId}",
                response,
            )
        } catch (exception: Exception) {
            // TODO: Add a dedicated WebSocket error response flow for STOMP clients.
            log.warn("Failed to send WebSocket chat message. chatRoomId={}", request.chatRoomId, exception)
            throw exception
        }
    }

    private fun Principal.extractUserPrincipal(): CustomUserPrincipal {
        val authentication = this as? Authentication
            ?: throw AuthenticationCredentialsNotFoundException("WebSocket authentication is required.")

        return authentication.principal as? CustomUserPrincipal
            ?: throw AuthenticationCredentialsNotFoundException("WebSocket user principal is required.")
    }

    private fun SendChatMessageRequest.toCommand(senderId: UUID): SendChatMessageCommand {
        return SendChatMessageCommand(
            senderId = senderId,
            chatRoomId = chatRoomId,
            content = content,
        )
    }

    private fun SendChatMessageResult.toResponse(): ChatMessageResponse {
        return ChatMessageResponse(
            messageId = chatMessageId,
            chatRoomId = chatRoomId,
            senderId = sender.id,
            senderNickname = sender.nickname,
            messageType = messageType,
            content = content,
            createdAt = createdAt,
        )
    }
}
