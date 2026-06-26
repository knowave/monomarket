package com.knowave.monomarket.common.websocket.event

import com.knowave.monomarket.domains.auth.principal.CustomUserPrincipal
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Component
class WebSocketEventListener {
    private val log = LoggerFactory.getLogger(WebSocketEventListener::class.java)

    @EventListener
    fun handleConnect(event: SessionConnectEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        handleConnected(
            sessionId = accessor.sessionId,
            userId = accessor.user?.extractUserId(),
        )
    }

    @EventListener
    fun handleDisconnect(event: SessionDisconnectEvent) {
        handleDisconnected(
            sessionId = event.sessionId,
            userId = event.user?.extractUserId(),
        )
    }

    private fun handleConnected(
        sessionId: String?,
        userId: String?,
    ) {
        log.info("WebSocket connected. sessionId={}, userId={}", sessionId, userId)
    }

    private fun handleDisconnected(
        sessionId: String?,
        userId: String?,
    ) {
        log.info("WebSocket disconnected. sessionId={}, userId={}", sessionId, userId)
    }

    private fun java.security.Principal.extractUserId(): String? {
        val principal = (this as? org.springframework.security.core.Authentication)?.principal
        return (principal as? CustomUserPrincipal)?.userId?.toString()
    }
}
