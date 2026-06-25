package com.knowave.monomarket.domains.favorite.controller

import com.knowave.monomarket.domains.auth.principal.CustomUserPrincipal
import com.knowave.monomarket.domains.favorite.dto.FavoriteListRequest
import com.knowave.monomarket.domains.favorite.dto.FavoritePageResponse
import com.knowave.monomarket.domains.favorite.dto.FavoriteToggleRequest
import com.knowave.monomarket.domains.favorite.dto.FavoriteToggleResponse
import com.knowave.monomarket.domains.favorite.service.FavoriteService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/favorites")
class FavoriteController(
    private val favoriteService: FavoriteService,
) {
    @PostMapping
    fun toggleFavorite(
        @AuthenticationPrincipal principal: CustomUserPrincipal,
        @Valid @RequestBody request: FavoriteToggleRequest,
    ): FavoriteToggleResponse {
        return favoriteService.toggleFavorite(
            userId = principal.userId,
            request = request,
        )
    }

    @PostMapping("/list")
    fun getFavorites(
        @AuthenticationPrincipal principal: CustomUserPrincipal,
        @Valid @RequestBody request: FavoriteListRequest,
    ): FavoritePageResponse {
        return favoriteService.getFavorites(
            userId = principal.userId,
            request = request,
        )
    }
}
