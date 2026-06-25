package com.knowave.monomarket.domains.product.exception

import com.knowave.monomarket.common.exception.MonomarketException
import org.springframework.http.HttpStatus

object ProductExceptions {
    fun notFound(): MonomarketException {
        return MonomarketException(
            errorCode = "PRODUCT_NOT_FOUND",
            message = "Product was not found.",
            status = HttpStatus.NOT_FOUND,
        )
    }

    fun forbidden(): MonomarketException {
        return MonomarketException(
            errorCode = "PRODUCT_FORBIDDEN",
            message = "Only the product seller can perform this action.",
            status = HttpStatus.FORBIDDEN,
        )
    }

    fun userNotFound(): MonomarketException {
        return MonomarketException(
            errorCode = "USER_NOT_FOUND",
            message = "Authenticated user was not found.",
            status = HttpStatus.NOT_FOUND,
        )
    }

    fun invalidStatus(): MonomarketException {
        return MonomarketException(
            errorCode = "INVALID_PRODUCT_STATUS",
            message = "Product status must be one of ON_SALE, RESERVED, or SOLD_OUT.",
            status = HttpStatus.BAD_REQUEST,
        )
    }

    fun invalidProductField(message: String): MonomarketException {
        return MonomarketException(
            errorCode = "INVALID_PRODUCT_FIELD",
            message = message,
            status = HttpStatus.BAD_REQUEST,
        )
    }

    fun invalidTempImageKey(): MonomarketException {
        return MonomarketException(
            errorCode = "INVALID_TEMP_IMAGE_KEY",
            message = "Image key must be a valid temp product image key.",
            status = HttpStatus.BAD_REQUEST,
        )
    }

    fun forbiddenTempImageKey(): MonomarketException {
        return MonomarketException(
            errorCode = "FORBIDDEN_TEMP_IMAGE_KEY",
            message = "Image key must belong to the authenticated user's temp path.",
            status = HttpStatus.FORBIDDEN,
        )
    }

    fun s3MoveFailed(): MonomarketException {
        return MonomarketException(
            errorCode = "S3_OBJECT_MOVE_FAILED",
            message = "Failed to move product image object.",
            status = HttpStatus.INTERNAL_SERVER_ERROR,
        )
    }

    fun s3DeleteFailed(): MonomarketException {
        return MonomarketException(
            errorCode = "S3_OBJECT_DELETE_FAILED",
            message = "Failed to delete product image object.",
            status = HttpStatus.INTERNAL_SERVER_ERROR,
        )
    }
}
