package com.knowave.monomarket.common.websocket.security

import com.knowave.monomarket.domains.auth.jwt.JwtProvider
import com.knowave.monomarket.domains.auth.principal.CustomUserPrincipal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import java.util.UUID

@SpringBootTest
class WebSocketJwtChannelInterceptorTests @Autowired constructor(
    private val webSocketJwtChannelInterceptor: WebSocketJwtChannelInterceptor,
    private val jwtProvider: JwtProvider,
) {
    @Test
    fun `connect with access token stores authenticated principal`() {
        val userId = UUID.randomUUID()
        val accessor = connectAccessor("Bearer ${jwtProvider.generateAccessToken(userId)}")

        webSocketJwtChannelInterceptor.preSend(
            message = message(accessor),
            channel = testChannel,
        )

        val authentication = accessor.user as Authentication
        val principal = authentication.principal as CustomUserPrincipal
        assertEquals(userId, principal.userId)
        assertEquals(authentication, accessor.sessionAttributes?.get(WebSocketJwtChannelInterceptor.SESSION_AUTHENTICATION_KEY))
    }

    @Test
    fun `connect without authorization header is rejected`() {
        assertThrows<BadCredentialsException> {
            webSocketJwtChannelInterceptor.preSend(
                message = message(connectAccessor()),
                channel = testChannel,
            )
        }
    }

    @Test
    fun `connect with invalid token is rejected`() {
        assertThrows<BadCredentialsException> {
            webSocketJwtChannelInterceptor.preSend(
                message = message(connectAccessor("Bearer invalid-token")),
                channel = testChannel,
            )
        }
    }

    @Test
    fun `connect with refresh token is rejected`() {
        assertThrows<BadCredentialsException> {
            webSocketJwtChannelInterceptor.preSend(
                message = message(connectAccessor("Bearer ${jwtProvider.generateRefreshToken(UUID.randomUUID())}")),
                channel = testChannel,
            )
        }
    }

    @Test
    fun `non connect message passes through without authentication`() {
        val accessor = StompHeaderAccessor.create(StompCommand.SEND)

        val result = webSocketJwtChannelInterceptor.preSend(
            message = message(accessor),
            channel = testChannel,
        )

        assertNotNull(result)
    }

    private fun connectAccessor(authorizationHeader: String? = null): StompHeaderAccessor {
        val accessor = StompHeaderAccessor.create(StompCommand.CONNECT)
        accessor.sessionAttributes = mutableMapOf()
        authorizationHeader?.let {
            accessor.setNativeHeader(HttpHeaders.AUTHORIZATION, it)
        }

        return accessor
    }

    private fun message(accessor: StompHeaderAccessor): Message<ByteArray> {
        accessor.setLeaveMutable(true)
        return MessageBuilder.createMessage(ByteArray(0), accessor.messageHeaders)
    }

    private val testChannel = object : MessageChannel {
        override fun send(message: Message<*>): Boolean {
            return true
        }

        override fun send(
            message: Message<*>,
            timeout: Long,
        ): Boolean {
            return true
        }
    }
}
