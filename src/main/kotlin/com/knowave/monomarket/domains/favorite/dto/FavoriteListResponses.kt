package com.knowave.monomarket.domains.favorite.dto

import java.time.LocalDateTime
import java.util.UUID

data class FavoriteListItemResponse(
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

data class FavoritePageResponse(
    val content: List<FavoriteListItemResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
