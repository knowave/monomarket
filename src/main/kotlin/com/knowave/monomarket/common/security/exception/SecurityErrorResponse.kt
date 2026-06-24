package com.knowave.monomarket.common.security.exception

data class SecurityErrorResponse(
    val message: String,
) {
    fun toJson(): String = """{"message":"$message"}"""
}
