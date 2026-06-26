package com.knowave.monomarket.domains.favorite.repository

import com.knowave.monomarket.domains.favorite.entity.Favorite
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
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

    @Modifying
    @Query(
        value = """
            delete from favorites
            where user_id = :userId
        """,
        nativeQuery = true,
    )
    fun deleteManyFavoriteByUser(userId: UUID)

    @Modifying
    @Query(
        value = """
            delete from favorites favorite
            using products product
            where favorite.product_id = product.id
              and product.seller_id = :sellerId
        """,
        nativeQuery = true,
    )
    fun deleteManyFavoriteByProductSeller(sellerId: UUID)

    @EntityGraph(attributePaths = ["product", "product.seller"])
    fun findAllByUserId(
        userId: UUID,
        pageable: Pageable,
    ): Page<Favorite>
}
