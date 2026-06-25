package com.knowave.monomarket.domains.product.controller

import com.knowave.monomarket.domains.auth.principal.CustomUserPrincipal
import com.knowave.monomarket.domains.product.dto.ProductCreateRequest
import com.knowave.monomarket.domains.product.dto.ProductCreateResponse
import com.knowave.monomarket.domains.product.dto.ProductDetailResponse
import com.knowave.monomarket.domains.product.dto.ProductPageResponse
import com.knowave.monomarket.domains.product.dto.ProductSearchRequest
import com.knowave.monomarket.domains.product.dto.ProductUpdateRequest
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
        val response = productService.createProduct(
            userId = principal.userId,
            request = request,
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{productId}")
    fun getProduct(
        @PathVariable productId: UUID,
    ): ProductDetailResponse {
        return productService.getProduct(productId)
    }

    @GetMapping
    fun getProducts(
        @Valid @ModelAttribute request: ProductSearchRequest,
    ): ProductPageResponse {
        return productService.getProducts(request)
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
            request = request,
        )
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
}
