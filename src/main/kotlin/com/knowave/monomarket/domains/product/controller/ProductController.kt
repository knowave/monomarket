package com.knowave.monomarket.domains.product.controller

import com.knowave.monomarket.domains.auth.principal.CustomUserPrincipal
import com.knowave.monomarket.domains.product.dto.GetManyProductByQueryCommand
import com.knowave.monomarket.domains.product.dto.GetManyProductByQueryItemResponse
import com.knowave.monomarket.domains.product.dto.GetManyProductByQueryRequest
import com.knowave.monomarket.domains.product.dto.GetManyProductByQueryResponse
import com.knowave.monomarket.domains.product.dto.GetManyProductByQueryResult
import com.knowave.monomarket.domains.product.dto.GetManyProductCommand
import com.knowave.monomarket.domains.product.dto.ProductCreateCommand
import com.knowave.monomarket.domains.product.dto.ProductCreateRequest
import com.knowave.monomarket.domains.product.dto.ProductCreateResponse
import com.knowave.monomarket.domains.product.dto.ProductCreateResult
import com.knowave.monomarket.domains.product.dto.ProductDetailResponse
import com.knowave.monomarket.domains.product.dto.ProductDetailResult
import com.knowave.monomarket.domains.product.dto.ProductListItemResponse
import com.knowave.monomarket.domains.product.dto.ProductPageResponse
import com.knowave.monomarket.domains.product.dto.ProductPageResult
import com.knowave.monomarket.domains.product.dto.ProductSearchRequest
import com.knowave.monomarket.domains.product.dto.ProductUpdateCommand
import com.knowave.monomarket.domains.product.dto.ProductUpdateRequest
import com.knowave.monomarket.domains.product.dto.SellerSummaryResponse
import com.knowave.monomarket.domains.product.dto.SellerSummaryResult
import com.knowave.monomarket.domains.product.service.ProductService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val productService: ProductService,
) {
    @PostMapping
    fun createProduct(
        @AuthenticationPrincipal principal: CustomUserPrincipal,
        @Valid @RequestBody request: ProductCreateRequest,
    ): ResponseEntity<ProductCreateResponse> {
        val result = productService.createProduct(
            userId = principal.userId,
            command = request.toCommand(),
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(result.toResponse())
    }

    @GetMapping("/{productId}")
    fun getProduct(
        @PathVariable productId: UUID,
    ): ProductDetailResponse {
        return productService.getProduct(productId).toResponse()
    }

    @GetMapping
    fun getManyProduct(
        @Valid @ModelAttribute request: ProductSearchRequest,
    ): ProductPageResponse {
        return productService.getManyProduct(request.toCommand()).toResponse()
    }

    @GetMapping("/query")
    fun getManyProductByQuery(
        @AuthenticationPrincipal principal: CustomUserPrincipal?,
        @Valid @ModelAttribute request: GetManyProductByQueryRequest,
    ): GetManyProductByQueryResponse {
        return productService.getManyProductByQuery(
            userId = principal?.userId,
            command = request.toCommand(),
        ).toResponse()
    }

    @PatchMapping("/{productId}")
    fun updateProduct(
        @AuthenticationPrincipal principal: CustomUserPrincipal,
        @PathVariable productId: UUID,
        @Valid @RequestBody request: ProductUpdateRequest,
    ): ProductDetailResponse {
        return productService.updateProduct(
            productId = productId,
            userId = principal.userId,
            command = request.toCommand(),
        ).toResponse()
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProduct(
        @AuthenticationPrincipal principal: CustomUserPrincipal,
        @PathVariable productId: UUID,
    ) {
        productService.deleteProduct(
            productId = productId,
            userId = principal.userId,
        )
    }

    private fun ProductCreateRequest.toCommand(): ProductCreateCommand {
        return ProductCreateCommand(
            title = title,
            description = description,
            price = price,
            imageKeys = imageKeys,
        )
    }

    private fun ProductUpdateRequest.toCommand(): ProductUpdateCommand {
        return ProductUpdateCommand(
            title = title,
            description = description,
            price = price,
            status = status,
            imageKeys = imageKeys,
        )
    }

    private fun ProductSearchRequest.toCommand(): GetManyProductCommand {
        return GetManyProductCommand(
            keyword = keyword,
            status = status,
            page = page,
            size = size,
            sort = sort,
        )
    }

    private fun GetManyProductByQueryRequest.toCommand(): GetManyProductByQueryCommand {
        return GetManyProductByQueryCommand(
            page = page,
            size = size,
        )
    }

    private fun ProductCreateResult.toResponse(): ProductCreateResponse {
        return ProductCreateResponse(
            id = id,
            title = title,
            description = description,
            price = price,
            status = status,
            sellerId = sellerId,
            imageUrls = imageUrls,
            createdAt = createdAt,
        )
    }

    private fun ProductDetailResult.toResponse(): ProductDetailResponse {
        return ProductDetailResponse(
            id = id,
            title = title,
            description = description,
            price = price,
            status = status,
            seller = seller.toResponse(),
            imageUrls = imageUrls,
            viewCount = viewCount,
            favoriteCount = favoriteCount,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun ProductPageResult.toResponse(): ProductPageResponse {
        return ProductPageResponse(
            items = items.map {
                ProductListItemResponse(
                    id = it.id,
                    title = it.title,
                    price = it.price,
                    status = it.status,
                    seller = it.seller.toResponse(),
                    thumbnailUrl = it.thumbnailUrl,
                    viewCount = it.viewCount,
                    favoriteCount = it.favoriteCount,
                    createdAt = it.createdAt,
                )
            },
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
            hasNext = hasNext,
        )
    }

    private fun GetManyProductByQueryResult.toResponse(): GetManyProductByQueryResponse {
        return GetManyProductByQueryResponse(
            content = content.map {
                GetManyProductByQueryItemResponse(
                    productId = it.productId,
                    title = it.title,
                    price = it.price,
                    thumbnailUrl = it.thumbnailUrl,
                    favoriteCount = it.favoriteCount,
                    viewCount = it.viewCount,
                    status = it.status,
                    sellerNickname = it.sellerNickname,
                    createdAt = it.createdAt,
                    isFavorite = it.isFavorite,
                )
            },
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
            hasNext = hasNext,
        )
    }

    private fun SellerSummaryResult.toResponse(): SellerSummaryResponse {
        return SellerSummaryResponse(
            id = id,
            nickname = nickname,
        )
    }
}
