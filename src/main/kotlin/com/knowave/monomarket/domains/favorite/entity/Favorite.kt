package com.knowave.monomarket.domains.favorite.entity

import com.knowave.monomarket.common.entity.BaseEntity
import com.knowave.monomarket.domains.product.entity.Product
import com.knowave.monomarket.domains.user.entity.User
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "favorites",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_favorite_user_product",
            columnNames = ["user_id", "product_id"],
        )
    ],
)
class Favorite(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,
) : BaseEntity()
