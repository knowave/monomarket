package com.knowave.monomarket.domains.chat.dto

import jakarta.validation.constraints.Min

data class GetManyChatMessageRequest(
    @field:Min(value = 0, message = "Page must be greater than or equal to 0.")
    val page: Int = 0,

    @field:Min(value = 1, message = "Size must be greater than or equal to 1.")
    val size: Int = 30,
)
