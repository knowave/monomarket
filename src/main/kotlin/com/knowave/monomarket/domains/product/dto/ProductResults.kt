package com.knowave.monomarket.domains.product.dto

import java.time.LocalDateTime
import java.util.UUID

data class ProductCreateResult(
    val id: UUID,
    val title: String,
    val description: String,
    val price: Long,
    val status: String,
    val sellerId: UUID,
    val imageUrls: List<String>,
    val createdAt: LocalDateTime,
)

data class ProductDetailResult(
    val id: UUID,
    val title: String,
    val description: String,
    val price: Long,
    val status: String,
    val seller: SellerSummaryResult,
    val imageUrls: List<String>,
    val viewCount: Long,
    val favoriteCount: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

data class ProductListItemResult(
    val id: UUID,
    val title: String,
    val price: Long,
    val status: String,
    val seller: SellerSummaryResult,
    val thumbnailUrl: String?,
    val viewCount: Long,
    val favoriteCount: Long,
    val createdAt: LocalDateTime,
)

data class ProductPageResult(
    val items: List<ProductListItemResult>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class GetManyProductByQueryItemResult(
    val productId: UUID,
    val title: String,
    val price: Long,
    val thumbnailUrl: String?,
    val favoriteCount: Long,
    val viewCount: Long,
    val status: String,
    val sellerNickname: String,
    val createdAt: LocalDateTime,
    val isFavorite: Boolean,
)

data class GetManyProductByQueryResult(
    val content: List<GetManyProductByQueryItemResult>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class SellerSummaryResult(
    val id: UUID,
    val nickname: String,
)
