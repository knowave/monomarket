package com.knowave.monomarket.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "monomarket.aws")
data class S3Properties(
    val region: String,
    val accessKey: String,
    val secretKey: String,
    val s3: S3BucketProperties,
)

data class S3BucketProperties(
    val bucket: String,
    val cdnBaseUrl: String,
)
