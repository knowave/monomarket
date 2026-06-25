package com.knowave.monomarket.common.security.config

import com.knowave.monomarket.common.security.filter.JwtAuthenticationFilter
import com.knowave.monomarket.common.security.handler.RestAccessDeniedHandler
import com.knowave.monomarket.common.security.handler.RestAuthenticationEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val authenticationEntryPoint: RestAuthenticationEntryPoint,
    private val accessDeniedHandler: RestAccessDeniedHandler,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .oauth2Login { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling {
                it.authenticationEntryPoint(authenticationEntryPoint)
                it.accessDeniedHandler(accessDeniedHandler)
            }
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.POST, "/api/v1/auth/social-login").permitAll()
                it.requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                it.requestMatchers(HttpMethod.GET, "/api/v1/products/query").permitAll()
                it.requestMatchers(HttpMethod.GET, "/api/v1/products", "/api/v1/products/*").permitAll()
                it.requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                it.anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}
