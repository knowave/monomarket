package com.knowave.monomarket.domains.product.service

import com.knowave.monomarket.common.config.S3Properties
import com.knowave.monomarket.common.enum.ProductStatus
import com.knowave.monomarket.domains.aws.service.S3Service
import com.knowave.monomarket.domains.product.dto.ProductCreateRequest
import com.knowave.monomarket.domains.product.dto.ProductCreateResponse
import com.knowave.monomarket.domains.product.dto.ProductDetailResponse
import com.knowave.monomarket.domains.product.dto.ProductListItemResponse
import com.knowave.monomarket.domains.product.dto.ProductPageResponse
import com.knowave.monomarket.domains.product.dto.ProductSearchRequest
import com.knowave.monomarket.domains.product.dto.ProductUpdateRequest
import com.knowave.monomarket.domains.product.dto.SellerSummaryResponse
import com.knowave.monomarket.domains.product.entity.Product
import com.knowave.monomarket.domains.product.entity.ProductImage
import com.knowave.monomarket.domains.product.exception.ProductExceptions
import com.knowave.monomarket.domains.product.repository.ProductRepository
import com.knowave.monomarket.domains.user.entity.User
import com.knowave.monomarket.domains.user.repository.UserRepository
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val s3Service: S3Service,
    private val s3Properties: S3Properties,
) {
    @Transactional
    fun createProduct(
        userId: UUID,
        request: ProductCreateRequest,
    ): ProductCreateResponse {
        val seller = userRepository.findById(userId)
            .orElseThrow { ProductExceptions.userNotFound() }
        val product = productRepository.save(
            Product(
                seller = seller,
                title = request.title,
                description = request.description,
                price = request.price,
                status = ProductStatus.ON_SALE.name,
            ),
        )
        val productId = requireNotNull(product.id)

        val images = moveTempImages(
            userId = userId,
            productId = productId,
            imageKeys = request.imageKeys.orEmpty(),
        )
        product.replaceImages(buildProductImages(product, images))
        // TODO: In production, use an outbox/retry queue to compensate if DB commit fails after S3 moves.

        return toCreateResponse(product)
    }

    @Transactional
    fun getProduct(productId: UUID): ProductDetailResponse {
        val product = findProduct(productId)
        product.increaseViewCount()

        return toDetailResponse(product)
    }

    @Transactional(readOnly = true)
    fun getProducts(request: ProductSearchRequest): ProductPageResponse {
        val pageable = PageRequest.of(
            request.page,
            request.size.coerceAtMost(100),
            resolveSort(request.sort),
        )
        val productStatus = request.status?.takeIf { it.isNotBlank() }?.let { parseStatus(it) }
        val products = productRepository.findAll(buildSpecification(request.keyword, productStatus), pageable)

        return ProductPageResponse(
            items = products.content.map { product ->
                toListItemResponse(product)
            },
            page = products.number,
            size = products.size,
            totalElements = products.totalElements,
            totalPages = products.totalPages,
            hasNext = products.hasNext(),
        )
    }

    @Transactional
    fun updateProduct(
        productId: UUID,
        userId: UUID,
        request: ProductUpdateRequest,
    ): ProductDetailResponse {
        val product = findProduct(productId)
        validateSeller(product, userId)

        request.title?.let {
            if (it.isBlank()) {
                throw ProductExceptions.invalidProductField("Title must not be blank.")
            }
            product.title = it
        }
        request.description?.let {
            if (it.isBlank()) {
                throw ProductExceptions.invalidProductField("Description must not be blank.")
            }
            product.description = it
        }
        request.price?.let { product.price = it }
        request.status?.let { product.status = parseStatus(it).name }

        if (request.imageKeys != null) {
            replaceImages(
                product = product,
                userId = userId,
                imageKeys = request.imageKeys,
            )
        }
        // TODO: In production, use an outbox/retry queue to compensate if DB commit fails after S3 moves/deletes.

        return toDetailResponse(product)
    }

    @Transactional
    fun deleteProduct(
        productId: UUID,
        userId: UUID,
    ) {
        val product = findProduct(productId)
        validateSeller(product, userId)
        val objectKeys = product.images.map { it.objectKey }

        deleteObjects(objectKeys)
        // TODO: In production, use an outbox/retry queue to retry S3 deletes and compensate failed DB deletes.
        productRepository.delete(product)
    }

    private fun replaceImages(
        product: Product,
        userId: UUID,
        imageKeys: List<String>,
    ) {
        val existingObjectKeys = product.images.map { it.objectKey }
        val productId = requireNotNull(product.id)
        val newObjectKeys = moveTempImages(
            userId = userId,
            productId = productId,
            imageKeys = imageKeys,
        )

        deleteObjects(existingObjectKeys)
        product.replaceImages(buildProductImages(product, newObjectKeys))
    }

    private fun buildProductImages(
        product: Product,
        objectKeys: List<String>,
    ): List<ProductImage> {
        return objectKeys.mapIndexed { index, objectKey ->
            ProductImage(
                product = product,
                objectKey = objectKey,
                sortOrder = index,
                isThumbnail = index == 0,
            )
        }
    }

    private fun moveTempImages(
        userId: UUID,
        productId: UUID,
        imageKeys: List<String>,
    ): List<String> {
        return imageKeys.map { imageKey ->
            validateTempImageKey(userId, imageKey)
            val targetKey = "products/$productId/${imageKey.substringAfterLast('/')}"
            try {
                s3Service.moveObject(imageKey, targetKey)
            } catch (exception: RuntimeException) {
                throw ProductExceptions.s3MoveFailed()
            }
            targetKey
        }
    }

    private fun deleteObjects(objectKeys: List<String>) {
        objectKeys.forEach { objectKey ->
            try {
                s3Service.deleteObject(objectKey)
            } catch (exception: RuntimeException) {
                throw ProductExceptions.s3DeleteFailed()
            }
        }
    }

    private fun validateTempImageKey(
        userId: UUID,
        imageKey: String,
    ) {
        if (!imageKey.startsWith("temp/products/")) {
            throw ProductExceptions.invalidTempImageKey()
        }
        if (!imageKey.startsWith("temp/products/$userId/")) {
            throw ProductExceptions.forbiddenTempImageKey()
        }
        if (imageKey.substringAfterLast('/').isBlank()) {
            throw ProductExceptions.invalidTempImageKey()
        }
    }

    private fun validateSeller(
        product: Product,
        userId: UUID,
    ) {
        if (product.seller.id != userId) {
            throw ProductExceptions.forbidden()
        }
    }

    private fun findProduct(productId: UUID): Product {
        return productRepository.findById(productId)
            .orElseThrow { ProductExceptions.notFound() }
    }

    private fun parseStatus(value: String): ProductStatus {
        return ProductStatus.entries.firstOrNull { it.name == value }
            ?: throw ProductExceptions.invalidStatus()
    }

    private fun resolveSort(sort: String?): Sort {
        return when (sort) {
            null, "", "latest" -> Sort.by(Sort.Direction.DESC, "createdAt")
            "price_asc" -> Sort.by(Sort.Direction.ASC, "price")
            "price_desc" -> Sort.by(Sort.Direction.DESC, "price")
            "view_count" -> Sort.by(Sort.Direction.DESC, "viewCount")
            else -> Sort.by(Sort.Direction.DESC, "createdAt")
        }
    }

    private fun buildSpecification(
        keyword: String?,
        status: ProductStatus?,
    ): Specification<Product> {
        return Specification { root, _, criteriaBuilder ->
            val predicates = mutableListOf<Predicate>()

            keyword?.trim()?.takeIf { it.isNotBlank() }?.let {
                val pattern = "%${it.lowercase()}%"
                predicates.add(
                    criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern),
                    ),
                )
            }
            status?.let {
                predicates.add(criteriaBuilder.equal(root.get<String>("status"), it.name))
            }

            criteriaBuilder.and(*predicates.toTypedArray())
        }
    }

    private fun toCreateResponse(product: Product): ProductCreateResponse {
        return ProductCreateResponse(
            id = requireNotNull(product.id),
            title = product.title,
            description = product.description,
            price = product.price,
            status = product.status,
            sellerId = requireNotNull(product.seller.id),
            imageUrls = sortedImageUrls(product),
            createdAt = requireNotNull(product.createdAt),
        )
    }

    private fun toDetailResponse(product: Product): ProductDetailResponse {
        return ProductDetailResponse(
            id = requireNotNull(product.id),
            title = product.title,
            description = product.description,
            price = product.price,
            status = product.status,
            seller = toSummaryResponse(product.seller),
            imageUrls = sortedImageUrls(product),
            viewCount = product.viewCount,
            favoriteCount = product.favoriteCount,
            createdAt = requireNotNull(product.createdAt),
            updatedAt = requireNotNull(product.updatedAt),
        )
    }

    private fun toListItemResponse(product: Product): ProductListItemResponse {
        return ProductListItemResponse(
            id = requireNotNull(product.id),
            title = product.title,
            price = product.price,
            status = product.status,
            seller = toSummaryResponse(product.seller),
            thumbnailUrl = product.images
                .minByOrNull { it.sortOrder }
                ?.let { buildImageUrl(it.objectKey) },
            viewCount = product.viewCount,
            favoriteCount = product.favoriteCount,
            createdAt = requireNotNull(product.createdAt),
        )
    }

    private fun sortedImageUrls(product: Product): List<String> {
        return product.images
            .sortedBy { it.sortOrder }
            .map { buildImageUrl(it.objectKey) }
    }

    private fun toSummaryResponse(user: User): SellerSummaryResponse {
        return SellerSummaryResponse(
            id = requireNotNull(user.id),
            nickname = user.nickname,
        )
    }

    private fun buildImageUrl(objectKey: String): String {
        return "${s3Properties.s3.cdnBaseUrl.trimEnd('/')}/$objectKey"
    }
}
