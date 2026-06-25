package com.knowave.monomarket.domains.product.dto

import java.time.LocalDateTime
import java.util.UUID

interface GetManyProductByQueryRow {
    val productId: UUID
    val title: String
    val price: Long
    val thumbnailObjectKey: String?
    val favoriteCount: Long
    val viewCount: Long
    val status: String
    val sellerNickname: String
    val createdAt: LocalDateTime
    val isFavorite: Boolean
    val totalElements: Long
}
