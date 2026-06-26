package com.knowave.monomarket.domains.favorite.controller

import com.knowave.monomarket.domains.auth.principal.CustomUserPrincipal
import com.knowave.monomarket.domains.favorite.dto.FavoriteListItemResponse
import com.knowave.monomarket.domains.favorite.dto.FavoriteListRequest
import com.knowave.monomarket.domains.favorite.dto.FavoritePageResponse
import com.knowave.monomarket.domains.favorite.dto.FavoritePageResult
import com.knowave.monomarket.domains.favorite.dto.GetManyFavoriteCommand
import com.knowave.monomarket.domains.favorite.dto.ProductFavoriteToggleCommand
import com.knowave.monomarket.domains.favorite.dto.ProductFavoriteToggleRequest
import com.knowave.monomarket.domains.favorite.dto.ProductFavoriteToggleResponse
import com.knowave.monomarket.domains.favorite.dto.ProductFavoriteToggleResult
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
    @PostMapping("/products")
    fun toggleProductFavorite(
        @AuthenticationPrincipal principal: CustomUserPrincipal,
        @Valid @RequestBody request: ProductFavoriteToggleRequest,
    ): ProductFavoriteToggleResponse {
        return favoriteService.toggleProductFavorite(
            userId = principal.userId,
            command = request.toCommand(),
        ).toResponse()
    }

    @PostMapping("/list")
    fun getManyFavorite(
        @AuthenticationPrincipal principal: CustomUserPrincipal,
        @Valid @RequestBody request: FavoriteListRequest,
    ): FavoritePageResponse {
        return favoriteService.getManyFavorite(
            userId = principal.userId,
            command = request.toCommand(),
        ).toResponse()
    }

    private fun ProductFavoriteToggleRequest.toCommand(): ProductFavoriteToggleCommand {
        return ProductFavoriteToggleCommand(productId = productId)
    }

    private fun FavoriteListRequest.toCommand(): GetManyFavoriteCommand {
        return GetManyFavoriteCommand(
            page = page,
            size = size,
        )
    }

    private fun ProductFavoriteToggleResult.toResponse(): ProductFavoriteToggleResponse {
        return ProductFavoriteToggleResponse(
            isFavorite = isFavorite,
            favoriteCount = favoriteCount,
        )
    }

    private fun FavoritePageResult.toResponse(): FavoritePageResponse {
        return FavoritePageResponse(
            content = content.map {
                FavoriteListItemResponse(
                    productId = it.productId,
                    title = it.title,
                    price = it.price,
                    thumbnailUrl = it.thumbnailUrl,
                    favoriteCount = it.favoriteCount,
                    viewCount = it.viewCount,
                    status = it.status,
                    sellerNickname = it.sellerNickname,
                    createdAt = it.createdAt,
                )
            },
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
        )
    }
}
