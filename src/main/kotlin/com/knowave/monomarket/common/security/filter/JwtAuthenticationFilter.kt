package com.knowave.monomarket.common.security.filter

import com.knowave.monomarket.domains.auth.jwt.JwtProvider
import com.knowave.monomarket.domains.auth.jwt.JwtTokenType
import com.knowave.monomarket.domains.auth.principal.CustomUserPrincipal
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = extractBearerToken(request)
        if (token != null && jwtProvider.validateToken(token)) {
            val tokenType = jwtProvider.extractTokenType(token)
            if (tokenType == JwtTokenType.ACCESS) {
                val principal = CustomUserPrincipal(userId = jwtProvider.extractUserId(token))
                val authentication = UsernamePasswordAuthenticationToken(principal, token, emptyList())
                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun extractBearerToken(request: HttpServletRequest): String? {
        val authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION) ?: return null
        if (!authorizationHeader.startsWith(BEARER_PREFIX, ignoreCase = true)) {
            return null
        }

        return authorizationHeader.substring(BEARER_PREFIX.length).trim().takeIf { it.isNotBlank() }
    }

    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }
}
