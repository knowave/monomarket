package com.knowave.monomarket.domains.favorite.repository

import com.knowave.monomarket.domains.favorite.entity.Favorite
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FavoriteRepository : JpaRepository<Favorite, UUID> {
    fun findByUserIdAndProductId(
        userId: UUID,
        productId: UUID,
    ): Favorite?

    fun existsByUserIdAndProductId(
        userId: UUID,
        productId: UUID,
    ): Boolean

    fun countByProductId(productId: UUID): Long

    @EntityGraph(attributePaths = ["product", "product.seller"])
    fun findAllByUserId(
        userId: UUID,
        pageable: Pageable,
    ): Page<Favorite>
}
