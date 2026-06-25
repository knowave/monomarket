package com.knowave.monomarket.domains.product.repository

import com.knowave.monomarket.domains.product.entity.Product
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import java.util.UUID

interface ProductRepository : JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    @EntityGraph(attributePaths = ["seller"])
    override fun findAll(
        spec: Specification<Product>,
        pageable: Pageable,
    ): Page<Product>
}
