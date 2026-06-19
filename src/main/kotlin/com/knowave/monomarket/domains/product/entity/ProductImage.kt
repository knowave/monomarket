package com.knowave.monomarket.domains.product.entity

import com.knowave.monomarket.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "product_images")
class ProductImage(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(nullable = false, length = 500)
    var imageUrl: String,

    @Column(nullable = false)
    var sortOrder: Int,

    @Column(nullable = false)
    var isThumbnail: Boolean = false,
) : BaseEntity()