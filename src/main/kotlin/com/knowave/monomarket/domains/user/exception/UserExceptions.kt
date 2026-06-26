package com.knowave.monomarket.domains.user.exception

import com.knowave.monomarket.common.exception.MonomarketException
import org.springframework.http.HttpStatus

object UserExceptions {
    fun notFound(): MonomarketException {
        return MonomarketException(
            errorCode = "USER_NOT_FOUND",
            message = "User not found.",
            status = HttpStatus.NOT_FOUND,
        )
    }

    fun invalidNickname(message: String): MonomarketException {
        return MonomarketException(
            errorCode = "INVALID_NICKNAME",
            message = message,
            status = HttpStatus.BAD_REQUEST,
        )
    }

    fun nicknameAlreadyExists(): MonomarketException {
        return MonomarketException(
            errorCode = "NICKNAME_ALREADY_EXISTS",
            message = "Nickname already exists.",
            status = HttpStatus.CONFLICT,
        )
    }
}
