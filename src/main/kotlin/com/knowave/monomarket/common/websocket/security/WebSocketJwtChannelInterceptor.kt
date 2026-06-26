package com.knowave.monomarket.common.websocket.security

import com.knowave.monomarket.domains.auth.jwt.JwtProvider
import com.knowave.monomarket.domains.auth.jwt.JwtTokenType
import com.knowave.monomarket.domains.auth.principal.CustomUserPrincipal
import org.springframework.http.HttpHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component

@Component
class WebSocketJwtChannelInterceptor(
    private val jwtProvider: JwtProvider,
) : ChannelInterceptor {
    override fun preSend(
        message: Message<*>,
        channel: MessageChannel,
    ): Message<*> {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
            ?: StompHeaderAccessor.wrap(message)

        if (accessor.command == StompCommand.CONNECT) {
            val token = extractBearerToken(accessor)
                ?: throw BadCredentialsException("WebSocket Authorization header is required.")

            if (!jwtProvider.validateToken(token) || jwtProvider.extractTokenType(token) != JwtTokenType.ACCESS) {
                throw BadCredentialsException("Invalid WebSocket access token.")
            }

            val principal = CustomUserPrincipal(userId = jwtProvider.extractUserId(token))
            val authentication = UsernamePasswordAuthenticationToken(principal, token, emptyList())
            accessor.user = authentication
            accessor.sessionAttributes?.put(SESSION_AUTHENTICATION_KEY, authentication)
        }

        return message
    }

    private fun extractBearerToken(accessor: StompHeaderAccessor): String? {
        val authorizationHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION) ?: return null
        if (!authorizationHeader.startsWith(BEARER_PREFIX, ignoreCase = true)) {
            return null
        }

        return authorizationHeader.substring(BEARER_PREFIX.length).trim().takeIf { it.isNotBlank() }
    }

    companion object {
        const val SESSION_AUTHENTICATION_KEY = "AUTHENTICATION"
        private const val BEARER_PREFIX = "Bearer "
    }
}
