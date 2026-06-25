package com.knowave.monomarket.domains.favorite.dto

data class ProductFavoriteToggleResponse(
    val isFavorite: Boolean,
    val favoriteCount: Long,
)
