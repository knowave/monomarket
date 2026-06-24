package com.knowave.monomarket.domains.upload.service

import com.knowave.monomarket.common.config.S3Properties
import com.knowave.monomarket.domains.aws.service.S3Service
import com.knowave.monomarket.domains.upload.dto.UploadImageResponse
import com.knowave.monomarket.domains.upload.exception.UploadExceptions
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class UploadService(
    private val s3Service: S3Service,
    private val properties: S3Properties,
) {
    fun uploadTempImage(
        userId: UUID,
        file: MultipartFile,
    ): UploadImageResponse {
        validateImageFile(file)

        val extension = extractExtension(file)
        val objectKey = "temp/$userId/${UUID.randomUUID()}.$extension"
        val uploadedObjectKey = s3Service.upload(objectKey, file)

        return UploadImageResponse(
            objectKey = uploadedObjectKey,
            imageUrl = buildImageUrl(uploadedObjectKey),
        )
    }

    private fun validateImageFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw UploadExceptions.emptyFile()
        }
        if (file.size > MAX_FILE_SIZE_BYTES) {
            throw UploadExceptions.fileTooLarge()
        }
        if (extractExtension(file) !in ALLOWED_EXTENSIONS) {
            throw UploadExceptions.unsupportedExtension()
        }
    }

    private fun extractExtension(file: MultipartFile): String {
        return file.originalFilename
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.lowercase()
            ?.takeIf { it.isNotBlank() }
            ?: throw UploadExceptions.unsupportedExtension()
    }

    private fun buildImageUrl(objectKey: String): String {
        return "${properties.s3.cdnBaseUrl.trimEnd('/')}/$objectKey"
    }

    companion object {
        private const val MAX_FILE_SIZE_BYTES = 10L * 1024L * 1024L
        private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")
    }
}
