package com.knowave.monomarket.domains.user.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateNicknameRequest(
    @field:NotBlank(message = "Nickname must not be blank.")
    @field:Size(max = 50, message = "Nickname must be less than or equal to 50 characters.")
    val nickname: String,
)
