package com.knowave.monomarket.domains.favorite.service

import com.knowave.monomarket.common.config.S3Properties
import com.knowave.monomarket.domains.favorite.dto.FavoriteListItemResult
import com.knowave.monomarket.domains.favorite.dto.FavoritePageResult
import com.knowave.monomarket.domains.favorite.dto.GetManyFavoriteCommand
import com.knowave.monomarket.domains.favorite.dto.ProductFavoriteToggleCommand
import com.knowave.monomarket.domains.favorite.dto.ProductFavoriteToggleResult
import com.knowave.monomarket.domains.favorite.entity.Favorite
import com.knowave.monomarket.domains.favorite.repository.FavoriteRepository
import com.knowave.monomarket.domains.product.entity.Product
import com.knowave.monomarket.domains.product.service.ProductService
import com.knowave.monomarket.domains.user.entity.User
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
    fun toggleProductFavorite(
        userId: UUID,
        command: ProductFavoriteToggleCommand,
    ): ProductFavoriteToggleResult {
        val user = userService.getUser(userId)
        val product = productService.getProductForFavoriteUpdate(command.productId)
        // TODO: Exclude soft-deleted products when product soft delete is introduced.
        val isFavorite = toggleProductFavorite(
            user = user,
            product = product,
        )

        if (isFavorite) {
            product.increaseFavoriteCount()
        } else {
            product.decreaseFavoriteCount()
        }

        return ProductFavoriteToggleResult(
            isFavorite = isFavorite,
            favoriteCount = product.favoriteCount,
        )
    }

    private fun toggleProductFavorite(
        user: User,
        product: Product,
    ): Boolean {
        val userId = requireNotNull(user.id)
        val productId = requireNotNull(product.id)
        val favorite = favoriteRepository.findByUserIdAndProductId(
            userId = userId,
            productId = productId,
        )

        if (favorite != null) {
            favoriteRepository.delete(favorite)
            return false
        }

        favoriteRepository.save(
            Favorite(
                user = user,
                product = product,
            ),
        )

        return true
    }

    @Transactional(readOnly = true)
    fun getManyFavorite(
        userId: UUID,
        command: GetManyFavoriteCommand,
    ): FavoritePageResult {
        userService.getUser(userId)

        val pageable = PageRequest.of(
            command.page,
            command.size.coerceAtMost(100),
            Sort.by(Sort.Direction.DESC, "createdAt"),
        )
        val favorites = favoriteRepository.findAllByUserId(userId, pageable)

        return FavoritePageResult(
            content = favorites.content.map { favorite ->
                toListItemResponse(favorite.product)
            },
            page = favorites.number,
            size = favorites.size,
            totalElements = favorites.totalElements,
            totalPages = favorites.totalPages,
        )
    }

    @Transactional
    fun deleteManyFavoriteByUser(userId: UUID) {
        favoriteRepository.deleteManyFavoriteByUser(userId)
    }

    @Transactional
    fun deleteManyFavoriteByProductSeller(sellerId: UUID) {
        favoriteRepository.deleteManyFavoriteByProductSeller(sellerId)
    }

    private fun toListItemResponse(product: Product): FavoriteListItemResult {
        return FavoriteListItemResult(
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
