package com.knowave.monomarket.domains.product.dto

import java.time.LocalDateTime
import java.util.UUID

data class ProductCreateResponse(
    val id: UUID,
    val title: String,
    val description: String,
    val price: Long,
    val status: String,
    val sellerId: UUID,
    val imageUrls: List<String>,
    val createdAt: LocalDateTime,
)

data class ProductDetailResponse(
    val id: UUID,
    val title: String,
    val description: String,
    val price: Long,
    val status: String,
    val seller: SellerSummaryResponse,
    val imageUrls: List<String>,
    val viewCount: Long,
    val favoriteCount: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

data class ProductListItemResponse(
    val id: UUID,
    val title: String,
    val price: Long,
    val status: String,
    val seller: SellerSummaryResponse,
    val thumbnailUrl: String?,
    val viewCount: Long,
    val favoriteCount: Long,
    val createdAt: LocalDateTime,
)

data class ProductPageResponse(
    val items: List<ProductListItemResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class SellerSummaryResponse(
    val id: UUID,
    val nickname: String,
)
