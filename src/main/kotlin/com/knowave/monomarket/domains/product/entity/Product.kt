package com.knowave.monomarket.domains.product.entity

import com.knowave.monomarket.common.entity.BaseEntity
import com.knowave.monomarket.common.enum.ProductStatus
import com.knowave.monomarket.domains.user.entity.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.BatchSize

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
    var status: String = ProductStatus.ON_SALE.name,

    @Column(nullable = false)
    var viewCount: Long = 0,

    @Column(nullable = false)
    var favoriteCount: Long = 0,

    @OneToMany(
        mappedBy = "product",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    @BatchSize(size = 100)
    val images: MutableList<ProductImage> = mutableListOf(),
) : BaseEntity() {

    fun increaseViewCount() {
        viewCount += 1
    }

    fun increaseFavoriteCount() {
        favoriteCount += 1
    }

    fun replaceImages(newImages: List<ProductImage>) {
        images.clear()
        images.addAll(newImages)
    }
}
