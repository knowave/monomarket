package com.knowave.monomarket.domains.chat.controller

import com.knowave.monomarket.domains.auth.principal.CustomUserPrincipal
import com.knowave.monomarket.domains.chat.dto.ChatParticipantResponse
import com.knowave.monomarket.domains.chat.dto.ChatParticipantResult
import com.knowave.monomarket.domains.chat.dto.SendChatMessageCommand
import com.knowave.monomarket.domains.chat.dto.SendChatMessageRequest
import com.knowave.monomarket.domains.chat.dto.SendChatMessageResponse
import com.knowave.monomarket.domains.chat.dto.SendChatMessageResult
import com.knowave.monomarket.domains.chat.service.ChatService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class ChatWebSocketController(
    private val chatService: ChatService,
) {
    @MessageMapping("/chat.send")
    @SendToUser("/queue/chat.send")
    fun sendChatMessage(
        principal: Principal,
        @Payload request: SendChatMessageRequest,
    ): SendChatMessageResponse {
        val userId = principal.extractUserPrincipal().userId
        return chatService.sendChatMessage(
            SendChatMessageCommand(
                senderId = userId,
                chatRoomId = request.chatRoomId,
                content = request.content,
            )
        ).toResponse()
    }

    private fun Principal.extractUserPrincipal(): CustomUserPrincipal {
        val authentication = this as? Authentication
            ?: throw AuthenticationCredentialsNotFoundException("WebSocket authentication is required.")

        return authentication.principal as? CustomUserPrincipal
            ?: throw AuthenticationCredentialsNotFoundException("WebSocket user principal is required.")
    }

    private fun SendChatMessageResult.toResponse(): SendChatMessageResponse {
        return SendChatMessageResponse(
            chatMessageId = chatMessageId,
            chatRoomId = chatRoomId,
            sender = sender.toResponse(),
            messageType = messageType,
            content = content,
            createdAt = createdAt,
        )
    }

    private fun ChatParticipantResult.toResponse(): ChatParticipantResponse {
        return ChatParticipantResponse(
            id = id,
            nickname = nickname,
        )
    }
}
