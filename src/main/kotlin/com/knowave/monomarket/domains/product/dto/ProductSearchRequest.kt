package com.knowave.monomarket.domains.product.dto

import jakarta.validation.constraints.Min

data class ProductSearchRequest(
    val keyword: String? = null,
    val status: String? = null,

    @field:Min(value = 0, message = "Page must be greater than or equal to 0.")
    val page: Int = 0,

    @field:Min(value = 1, message = "Size must be greater than or equal to 1.")
    val size: Int = 20,

    val sort: String? = null,
)
