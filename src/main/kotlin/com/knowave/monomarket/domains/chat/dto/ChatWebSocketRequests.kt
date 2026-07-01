package com.knowave.monomarket.domains.chat.dto

import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class SendChatMessageRequest(
    val chatRoomId: UUID,

    @field:NotBlank(message = "Chat message content must not be blank.")
    val content: String,
)
