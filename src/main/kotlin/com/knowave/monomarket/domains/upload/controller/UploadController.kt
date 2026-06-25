package com.knowave.monomarket.domains.upload.controller

import com.knowave.monomarket.domains.auth.principal.CustomUserPrincipal
import com.knowave.monomarket.domains.upload.dto.UploadImageResponse
import com.knowave.monomarket.domains.upload.dto.UploadImageResult
import com.knowave.monomarket.domains.upload.service.UploadService
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/uploads")
class UploadController(
    private val uploadService: UploadService,
) {
    @PostMapping(
        "/images",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    fun uploadImage(
        @AuthenticationPrincipal principal: CustomUserPrincipal,
        @RequestPart("file") file: MultipartFile,
    ): UploadImageResponse {
        return uploadService.uploadTempImage(
            userId = principal.userId,
            file = file,
        ).toResponse()
    }

    private fun UploadImageResult.toResponse(): UploadImageResponse {
        return UploadImageResponse(
            objectKey = objectKey,
            imageUrl = imageUrl,
        )
    }
}
