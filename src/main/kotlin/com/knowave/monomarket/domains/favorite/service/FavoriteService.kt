package com.knowave.monomarket.domains.favorite.service

import com.knowave.monomarket.common.config.S3Properties
import com.knowave.monomarket.domains.favorite.dto.FavoriteListItemResponse
import com.knowave.monomarket.domains.favorite.dto.FavoriteListRequest
import com.knowave.monomarket.domains.favorite.dto.FavoritePageResponse
import com.knowave.monomarket.domains.favorite.dto.FavoriteToggleRequest
import com.knowave.monomarket.domains.favorite.dto.FavoriteToggleResponse
import com.knowave.monomarket.domains.favorite.entity.Favorite
import com.knowave.monomarket.domains.favorite.repository.FavoriteRepository
import com.knowave.monomarket.domains.product.entity.Product
import com.knowave.monomarket.domains.product.service.ProductService
import com.knowave.monomarket.domains.user.service.UserService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class FavoriteService(
    private val favoriteRepository: FavoriteRepository,
    private val productService: ProductService,
    private val userService: UserService,
    private val s3Properties: S3Properties,
) {
    @Transactional
    fun toggleFavorite(
        userId: UUID,
        request: FavoriteToggleRequest,
    ): FavoriteToggleResponse {
        val user = userService.getUser(userId)
        val product = productService.getProductForFavoriteUpdate(request.productId)
        // TODO: Exclude soft-deleted products when product soft delete is introduced.

        val favorite = favoriteRepository.findByUserIdAndProductId(
            userId = userId,
            productId = request.productId,
        )

        if (favorite != null) {
            favoriteRepository.delete(favorite)
            product.decreaseFavoriteCount()

            return FavoriteToggleResponse(
                isFavorite = false,
                favoriteCount = product.favoriteCount,
            )
        }

        favoriteRepository.save(
            Favorite(
                user = user,
                product = product,
            ),
        )
        product.increaseFavoriteCount()

        return FavoriteToggleResponse(
            isFavorite = true,
            favoriteCount = product.favoriteCount,
        )
    }

    @Transactional(readOnly = true)
    fun getFavorites(
        userId: UUID,
        request: FavoriteListRequest,
    ): FavoritePageResponse {
        userService.getUser(userId)

        val pageable = PageRequest.of(
            request.page,
            request.size.coerceAtMost(100),
            Sort.by(Sort.Direction.DESC, "createdAt"),
        )
        val favorites = favoriteRepository.findAllByUserId(userId, pageable)

        return FavoritePageResponse(
            content = favorites.content.map { favorite ->
                toListItemResponse(favorite.product)
            },
            page = favorites.number,
            size = favorites.size,
            totalElements = favorites.totalElements,
            totalPages = favorites.totalPages,
        )
    }

    private fun toListItemResponse(product: Product): FavoriteListItemResponse {
        return FavoriteListItemResponse(
            productId = requireNotNull(product.id),
            title = product.title,
            price = product.price,
            thumbnailUrl = product.images
                .minByOrNull { it.sortOrder }
                ?.let { buildImageUrl(it.objectKey) },
            favoriteCount = product.favoriteCount,
            viewCount = product.viewCount,
            status = product.status,
            sellerNickname = product.seller.nickname,
            createdAt = requireNotNull(product.createdAt),
        )
    }

    private fun buildImageUrl(objectKey: String): String {
        return "${s3Properties.s3.cdnBaseUrl.trimEnd('/')}/$objectKey"
    }
}
