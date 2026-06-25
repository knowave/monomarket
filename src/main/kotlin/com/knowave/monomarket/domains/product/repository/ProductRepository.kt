package com.knowave.monomarket.domains.product.repository

import com.knowave.monomarket.domains.product.dto.GetManyProductByQueryRow
import com.knowave.monomarket.domains.product.entity.Product
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface ProductRepository : JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    @EntityGraph(attributePaths = ["seller"])
    override fun findAll(
        spec: Specification<Product>,
        pageable: Pageable,
    ): Page<Product>

    @Query(
        value = """
            select
                p.id as "productId",
                p.title as "title",
                p.price as "price",
                thumbnail.object_key as "thumbnailObjectKey",
                p.favorite_count as "favoriteCount",
                p.view_count as "viewCount",
                p.status as "status",
                seller.nickname as "sellerNickname",
                p.created_at as "createdAt",
                case when favorite.id is null then false else true end as "isFavorite",
                count(*) over() as "totalElements"
            from products p
            join users seller on seller.id = p.seller_id
            left join product_images thumbnail
                on thumbnail.product_id = p.id
               and thumbnail.sort_order = 0
            left join favorites favorite
                on favorite.product_id = p.id
               and (:userId is not null and favorite.user_id = :userId)
            where p.status = :status
            order by p.created_at desc
            limit :limit offset :offset
        """,
        nativeQuery = true,
    )
    fun findManyProductByQuery(
        status: String,
        userId: UUID?,
        limit: Int,
        offset: Long,
    ): List<GetManyProductByQueryRow>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :productId")
    fun findByIdForUpdate(productId: UUID): Product?
}
