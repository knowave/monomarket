package com.knowave.monomarket.domains.chat.exception

import com.knowave.monomarket.common.exception.MonomarketException
import org.springframework.http.HttpStatus

object ChatExceptions {
    fun chatRoomNotFound(): MonomarketException {
        return MonomarketException(
            errorCode = "CHAT_ROOM_NOT_FOUND",
            message = "Chat room was not found.",
            status = HttpStatus.NOT_FOUND,
        )
    }

    fun selfChatRoomForbidden(): MonomarketException {
        return MonomarketException(
            errorCode = "CHAT_ROOM_SELF_FORBIDDEN",
            message = "Seller cannot create a chat room for their own product.",
            status = HttpStatus.FORBIDDEN,
        )
    }

    fun chatRoomParticipantForbidden(): MonomarketException {
        return MonomarketException(
            errorCode = "CHAT_ROOM_PARTICIPANT_FORBIDDEN",
            message = "Only chat room participants can perform this action.",
            status = HttpStatus.FORBIDDEN,
        )
    }

    fun invalidMessageContent(): MonomarketException {
        return MonomarketException(
            errorCode = "INVALID_CHAT_MESSAGE_CONTENT",
            message = "Chat message content must not be blank.",
            status = HttpStatus.BAD_REQUEST,
        )
    }

    fun unsupportedMessageType(): MonomarketException {
        return MonomarketException(
            errorCode = "UNSUPPORTED_CHAT_MESSAGE_TYPE",
            message = "Chat message type is not supported.",
            status = HttpStatus.BAD_REQUEST,
        )
    }
}
