package com.knowave.monomarket.domains.chat.controller

import com.knowave.monomarket.domains.auth.principal.CustomUserPrincipal
import com.knowave.monomarket.domains.chat.dto.ChatMessageResponse
import com.knowave.monomarket.domains.chat.dto.ChatMessageResult
import com.knowave.monomarket.domains.chat.dto.ChatRoomSummaryResponse
import com.knowave.monomarket.domains.chat.dto.ChatRoomSummaryResult
import com.knowave.monomarket.domains.chat.dto.CreateOrGetChatRoomCommand
import com.knowave.monomarket.domains.chat.dto.CreateOrGetChatRoomRequest
import com.knowave.monomarket.domains.chat.dto.CreateOrGetChatRoomResponse
import com.knowave.monomarket.domains.chat.dto.CreateOrGetChatRoomResult
import com.knowave.monomarket.domains.chat.dto.GetManyChatMessageQuery
import com.knowave.monomarket.domains.chat.dto.GetManyChatMessageRequest
import com.knowave.monomarket.domains.chat.dto.GetManyChatMessageResponse
import com.knowave.monomarket.domains.chat.dto.GetManyChatMessageResult
import com.knowave.monomarket.domains.chat.dto.GetManyChatRoomQuery
import com.knowave.monomarket.domains.chat.dto.GetManyChatRoomRequest
import com.knowave.monomarket.domains.chat.dto.GetManyChatRoomResponse
import com.knowave.monomarket.domains.chat.dto.GetManyChatRoomResult
import com.knowave.monomarket.domains.chat.service.ChatService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/chat-rooms")
class ChatController(
    private val chatService: ChatService,
) {
    @PostMapping
    fun createOrGetChatRoom(
        @AuthenticationPrincipal principal: CustomUserPrincipal,
        @Valid @RequestBody request: CreateOrGetChatRoomRequest,
    ): CreateOrGetChatRoomResponse {
        return chatService.createOrGetChatRoom(request.toCommand(principal.userId)).toResponse()
    }

    @GetMapping
    fun getManyChatRoomByQuery(
        @AuthenticationPrincipal principal: CustomUserPrincipal,
        @Valid @ModelAttribute request: GetManyChatRoomRequest,
    ): GetManyChatRoomResponse {
        return chatService.getManyChatRoomByQuery(request.toQuery(principal.userId)).toResponse(principal.userId)
    }

    @GetMapping("/{chatRoomId}/messages")
    fun getManyChatMessageByQuery(
        @AuthenticationPrincipal principal: CustomUserPrincipal,
        @PathVariable chatRoomId: UUID,
        @Valid @ModelAttribute request: GetManyChatMessageRequest,
    ): GetManyChatMessageResponse {
        return chatService.getManyChatMessageByQuery(request.toQuery(chatRoomId, principal.userId)).toResponse()
    }

    private fun CreateOrGetChatRoomRequest.toCommand(requesterId: UUID): CreateOrGetChatRoomCommand {
        return CreateOrGetChatRoomCommand(
            requesterId = requesterId,
            productId = productId,
        )
    }

    private fun GetManyChatRoomRequest.toQuery(userId: UUID): GetManyChatRoomQuery {
        return GetManyChatRoomQuery(
            userId = userId,
            page = page,
            size = size,
        )
    }

    private fun GetManyChatMessageRequest.toQuery(
        chatRoomId: UUID,
        requesterId: UUID,
    ): GetManyChatMessageQuery {
        return GetManyChatMessageQuery(
            requesterId = requesterId,
            chatRoomId = chatRoomId,
            page = page,
            size = size,
        )
    }

    private fun CreateOrGetChatRoomResult.toResponse(): CreateOrGetChatRoomResponse {
        return CreateOrGetChatRoomResponse(
            chatRoomId = chatRoomId,
            productId = productId,
            buyerId = buyer.id,
            sellerId = seller.id,
            lastMessage = lastMessage,
            lastMessageAt = lastMessageAt,
            createdAt = createdAt,
        )
    }

    private fun GetManyChatRoomResult.toResponse(userId: UUID): GetManyChatRoomResponse {
        return GetManyChatRoomResponse(
            content = content.map { it.toResponse(userId) },
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
            hasNext = hasNext,
        )
    }

    private fun ChatRoomSummaryResult.toResponse(userId: UUID): ChatRoomSummaryResponse {
        val opponent = if (buyer.id == userId) seller else buyer

        return ChatRoomSummaryResponse(
            chatRoomId = chatRoomId,
            productId = productId,
            productTitle = productTitle,
            productThumbnailUrl = productThumbnailUrl,
            opponentUserId = opponent.id,
            opponentNickname = opponent.nickname,
            lastMessage = lastMessage,
            lastMessageAt = lastMessageAt,
            createdAt = createdAt,
        )
    }

    private fun GetManyChatMessageResult.toResponse(): GetManyChatMessageResponse {
        return GetManyChatMessageResponse(
            content = content.map { it.toResponse() },
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
            hasNext = hasNext,
        )
    }

    private fun ChatMessageResult.toResponse(): ChatMessageResponse {
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
