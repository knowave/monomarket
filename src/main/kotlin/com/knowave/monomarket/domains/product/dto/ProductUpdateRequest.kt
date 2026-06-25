package com.knowave.monomarket.domains.product.dto

import jakarta.validation.constraints.Positive

data class ProductUpdateRequest(
    val title: String? = null,
    val description: String? = null,

    @field:Positive(message = "Price must be greater than 0.")
    val price: Long? = null,

    val status: String? = null,
    val imageKeys: List<String>? = null,
)
