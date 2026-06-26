package com.knowave.monomarket.domains.product.service

import com.knowave.monomarket.common.config.S3Properties
import com.knowave.monomarket.common.enum.ProductStatus
import com.knowave.monomarket.domains.aws.service.S3Service
import com.knowave.monomarket.domains.product.dto.GetManyProductByQueryCommand
import com.knowave.monomarket.domains.product.dto.GetManyProductByQueryItemResult
import com.knowave.monomarket.domains.product.dto.GetManyProductByQueryResult
import com.knowave.monomarket.domains.product.dto.GetManyProductByQueryRow
import com.knowave.monomarket.domains.product.dto.GetManyProductCommand
import com.knowave.monomarket.domains.product.dto.ProductCreateCommand
import com.knowave.monomarket.domains.product.dto.ProductCreateResult
import com.knowave.monomarket.domains.product.dto.ProductDetailResult
import com.knowave.monomarket.domains.product.dto.ProductListItemResult
import com.knowave.monomarket.domains.product.dto.ProductPageResult
import com.knowave.monomarket.domains.product.dto.ProductUpdateCommand
import com.knowave.monomarket.domains.product.dto.SellerSummaryResult
import com.knowave.monomarket.domains.product.entity.Product
import com.knowave.monomarket.domains.product.entity.ProductImage
import com.knowave.monomarket.domains.product.exception.ProductExceptions
import com.knowave.monomarket.domains.product.repository.ProductRepository
import com.knowave.monomarket.domains.user.entity.User
import com.knowave.monomarket.domains.user.service.UserService
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
    private val userService: UserService,
    private val s3Service: S3Service,
    private val s3Properties: S3Properties,
) {
    @Transactional
    fun createProduct(
        userId: UUID,
        command: ProductCreateCommand,
    ): ProductCreateResult {
        val seller = userService.getUser(userId)
        val product = productRepository.save(
            Product(
                seller = seller,
                title = command.title,
                description = command.description,
                price = command.price,
                status = ProductStatus.ON_SALE.name,
            ),
        )
        val productId = requireNotNull(product.id)

        val images = moveTempImages(
            userId = userId,
            productId = productId,
            imageKeys = command.imageKeys.orEmpty(),
        )
        product.replaceImages(buildProductImages(product, images))
        // TODO: In production, use an outbox/retry queue to compensate if DB commit fails after S3 moves.

        return toCreateResponse(product)
    }

    @Transactional
    fun getProduct(productId: UUID): ProductDetailResult {
        val product = getProductById(productId)
        product.increaseViewCount()

        return toDetailResponse(product)
    }

    @Transactional(readOnly = true)
    fun getManyProduct(command: GetManyProductCommand): ProductPageResult {
        val pageable = PageRequest.of(
            command.page,
            command.size.coerceAtMost(100),
            resolveSort(command.sort),
        )
        val productStatus = command.status?.takeIf { it.isNotBlank() }?.let { parseStatus(it) }
        val products = productRepository.findAll(buildSpecification(command.keyword, productStatus), pageable)

        return ProductPageResult(
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

    @Transactional(readOnly = true)
    fun getManyProductByQuery(
        userId: UUID?,
        command: GetManyProductByQueryCommand,
    ): GetManyProductByQueryResult {
        val pageable = PageRequest.of(
            command.page,
            command.size.coerceAtMost(100),
            Sort.by(Sort.Direction.DESC, "createdAt"),
        )
        val products = productRepository.findManyProductByQuery(
            status = ProductStatus.ON_SALE.name,
            userId = userId,
            limit = pageable.pageSize,
            offset = pageable.offset,
        )
        val totalElements = products.firstOrNull()?.totalElements ?: 0
        val totalPages = calculateTotalPages(
            totalElements = totalElements,
            size = pageable.pageSize,
        )

        return GetManyProductByQueryResult(
            content = products.map { row -> toGetManyProductByQueryItemResult(row) },
            page = pageable.pageNumber,
            size = pageable.pageSize,
            totalElements = totalElements,
            totalPages = totalPages,
            hasNext = pageable.offset + products.size < totalElements,
        )
    }

    @Transactional
    fun getProductForFavoriteUpdate(productId: UUID): Product {
        return productRepository.findByIdForUpdate(productId)
            ?: throw ProductExceptions.notFound()
    }

    @Transactional
    fun updateProduct(
        productId: UUID,
        userId: UUID,
        command: ProductUpdateCommand,
    ): ProductDetailResult {
        val product = getProductById(productId)
        validateSeller(product, userId)

        command.title?.let {
            if (it.isBlank()) {
                throw ProductExceptions.invalidProductField("Title must not be blank.")
            }
            product.title = it
        }
        command.description?.let {
            if (it.isBlank()) {
                throw ProductExceptions.invalidProductField("Description must not be blank.")
            }
            product.description = it
        }
        command.price?.let { product.price = it }
        command.status?.let { product.status = parseStatus(it).name }

        if (command.imageKeys != null) {
            replaceImages(
                product = product,
                userId = userId,
                imageKeys = command.imageKeys,
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
        val product = getProductById(productId)
        validateSeller(product, userId)
        val objectKeys = product.images.map { it.objectKey }

        deleteObjects(objectKeys)
        // TODO: In production, use an outbox/retry queue to retry S3 deletes and compensate failed DB deletes.
        productRepository.delete(product)
    }

    @Transactional
    fun deleteManyProductBySeller(sellerId: UUID) {
        val products = productRepository.findManyProductBySellerId(sellerId)
        val objectKeys = products.flatMap { product ->
            product.images.map { image -> image.objectKey }
        }

        deleteObjects(objectKeys)
        productRepository.deleteAll(products)
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

    private fun getProductById(productId: UUID): Product {
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

    private fun toCreateResponse(product: Product): ProductCreateResult {
        return ProductCreateResult(
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

    private fun toDetailResponse(product: Product): ProductDetailResult {
        return ProductDetailResult(
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

    private fun toListItemResponse(product: Product): ProductListItemResult {
        return ProductListItemResult(
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

    private fun toGetManyProductByQueryItemResult(
        row: GetManyProductByQueryRow,
    ): GetManyProductByQueryItemResult {
        return GetManyProductByQueryItemResult(
            productId = row.productId,
            title = row.title,
            price = row.price,
            thumbnailUrl = row.thumbnailObjectKey?.let { buildImageUrl(it) },
            favoriteCount = row.favoriteCount,
            viewCount = row.viewCount,
            status = row.status,
            sellerNickname = row.sellerNickname,
            createdAt = row.createdAt,
            isFavorite = row.isFavorite,
        )
    }

    private fun calculateTotalPages(
        totalElements: Long,
        size: Int,
    ): Int {
        if (totalElements == 0L) {
            return 0
        }

        return ((totalElements + size - 1) / size).toInt()
    }

    private fun sortedImageUrls(product: Product): List<String> {
        return product.images
            .sortedBy { it.sortOrder }
            .map { buildImageUrl(it.objectKey) }
    }

    private fun toSummaryResponse(user: User): SellerSummaryResult {
        return SellerSummaryResult(
            id = requireNotNull(user.id),
            nickname = user.nickname,
        )
    }

    private fun buildImageUrl(objectKey: String): String {
        return "${s3Properties.s3.cdnBaseUrl.trimEnd('/')}/$objectKey"
    }
}
