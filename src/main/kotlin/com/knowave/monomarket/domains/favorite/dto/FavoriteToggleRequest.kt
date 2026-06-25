package com.knowave.monomarket.domains.favorite.dto

import java.util.UUID

data class FavoriteToggleRequest(
    val productId: UUID,
)
