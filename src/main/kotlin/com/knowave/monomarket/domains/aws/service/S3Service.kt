package com.knowave.monomarket.domains.aws.service

import com.knowave.monomarket.common.config.S3Properties
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@Service
class S3Service(
    private val s3Client: S3Client,
    private val properties: S3Properties,
) {
    fun upload(
        objectKey: String,
        file: MultipartFile,
    ): String {
        val request = PutObjectRequest.builder()
            .bucket(properties.s3.bucket)
            .key(objectKey)
            .contentType(file.contentType)
            .contentLength(file.size)
            .build()

        file.inputStream.use {
            s3Client.putObject(request, RequestBody.fromInputStream(it, file.size))
        }

        return objectKey
    }

    fun moveObject(
        sourceKey: String,
        targetKey: String,
    ) {
        val copyRequest = CopyObjectRequest.builder()
            .sourceBucket(properties.s3.bucket)
            .sourceKey(sourceKey)
            .destinationBucket(properties.s3.bucket)
            .destinationKey(targetKey)
            .build()
        s3Client.copyObject(copyRequest)

        val deleteRequest = DeleteObjectRequest.builder()
            .bucket(properties.s3.bucket)
            .key(sourceKey)
            .build()
        s3Client.deleteObject(deleteRequest)
    }
}
