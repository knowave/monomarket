package com.knowave.monomarket.domains.favorite.dto

data class FavoriteToggleResponse(
    val isFavorite: Boolean,
    val favoriteCount: Long,
)
