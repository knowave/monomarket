package com.knowave.monomarket.domains.favorite.dto

import java.time.LocalDateTime
import java.util.UUID

data class ProductFavoriteToggleResult(
    val isFavorite: Boolean,
    val favoriteCount: Long,
)

data class FavoriteListItemResult(
    val productId: UUID,
    val title: String,
    val price: Long,
    val thumbnailUrl: String?,
    val favoriteCount: Long,
    val viewCount: Long,
    val status: String,
    val sellerNickname: String,
    val createdAt: LocalDateTime,
)

data class FavoritePageResult(
    val content: List<FavoriteListItemResult>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
