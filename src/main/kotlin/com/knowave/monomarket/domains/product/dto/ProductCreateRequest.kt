package com.knowave.monomarket.domains.product.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class ProductCreateRequest(
    @field:NotBlank(message = "Title must not be blank.")
    val title: String,

    @field:NotBlank(message = "Description must not be blank.")
    val description: String,

    @field:Positive(message = "Price must be greater than 0.")
    val price: Long,

    val imageKeys: List<String>? = null,
)
