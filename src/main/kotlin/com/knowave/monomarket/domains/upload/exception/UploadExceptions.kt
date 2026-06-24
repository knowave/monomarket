package com.knowave.monomarket.domains.upload.exception

import com.knowave.monomarket.common.exception.MonomarketException
import org.springframework.http.HttpStatus

object UploadExceptions {
    fun emptyFile(): MonomarketException {
        return MonomarketException(
            errorCode = "EMPTY_UPLOAD_FILE",
            message = "Upload file must not be empty.",
            status = HttpStatus.BAD_REQUEST,
        )
    }

    fun fileTooLarge(): MonomarketException {
        return MonomarketException(
            errorCode = "UPLOAD_FILE_TOO_LARGE",
            message = "Upload file size must be less than or equal to 10MB.",
            status = HttpStatus.BAD_REQUEST,
        )
    }

    fun unsupportedExtension(): MonomarketException {
        return MonomarketException(
            errorCode = "UNSUPPORTED_UPLOAD_FILE_EXTENSION",
            message = "Only jpg, jpeg, png, and webp files are allowed.",
            status = HttpStatus.BAD_REQUEST,
        )
    }
}
