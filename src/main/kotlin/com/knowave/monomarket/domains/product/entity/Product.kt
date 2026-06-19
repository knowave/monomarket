package com.knowave.monomarket.domains.product.entity

import com.knowave.monomarket.common.entity.BaseEntity
import com.knowave.monomarket.common.enum.ProductStatus
import com.knowave.monomarket.domains.user.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "products")
class Product(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    val seller: User,

    @Column(nullable = false, length = 100)
    var title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,

    @Column(nullable = false)
    var price: Long,

    @Column(nullable = false)
    var status: String = ProductStatus.ON_SALE.name
) : BaseEntity()