package com.knowave.monomarket.domains.product.dto

data class ProductCreateCommand(
    val title: String,
    val description: String,
    val price: Long,
    val imageKeys: List<String>?,
)

data class ProductUpdateCommand(
    val title: String?,
    val description: String?,
    val price: Long?,
    val status: String?,
    val imageKeys: List<String>?,
)

data class GetManyProductCommand(
    val keyword: String?,
    val status: String?,
    val page: Int,
    val size: Int,
    val sort: String?,
)

data class GetManyProductByQueryCommand(
    val page: Int,
    val size: Int,
)
