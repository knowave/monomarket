package com.knowave.monomarket.domains.favorite.dto

import java.util.UUID

data class ProductFavoriteToggleRequest(
    val productId: UUID,
)
