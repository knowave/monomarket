package com.knowave.monomarket.common.exception

import org.springframework.http.HttpStatus

class MonomarketException(
    val errorCode: String,
    override val message: String,
    val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : RuntimeException(message) {
}