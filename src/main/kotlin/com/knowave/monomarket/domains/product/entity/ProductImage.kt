package com.knowave.monomarket.domains.product.entity

import com.knowave.monomarket.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "product_images",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_product_image_product_sort_order",
            columnNames = ["product_id", "sort_order"],
        ),
    ],
    indexes = [
        Index(
            name = "idx_product_image_product_thumbnail",
            columnList = "product_id, is_thumbnail",
        ),
    ],
)
class ProductImage(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(nullable = false, length = 500)
    var imageUrl: String,

    @Column(name = "sort_order", nullable = false)
    var sortOrder: Int,

    @Column(name = "is_thumbnail", nullable = false)
    var isThumbnail: Boolean = false,
) : BaseEntity()
