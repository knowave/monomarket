package com.knowave.monomarket.domains.product.repository

import com.knowave.monomarket.domains.product.entity.ProductImage
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProductImageRepository : JpaRepository<ProductImage, UUID> {
    fun findAllByProductIdOrderBySortOrderAsc(productId: UUID): List<ProductImage>

    fun findAllByProductIdInOrderBySortOrderAsc(productIds: Collection<UUID>): List<ProductImage>
}
