package com.knowave.monomarket.domains.favorite.dto

import java.util.UUID

data class ProductFavoriteToggleCommand(
    val productId: UUID,
)

data class GetManyFavoriteCommand(
    val page: Int,
    val size: Int,
)
