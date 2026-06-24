package com.knowave.monomarket.common.security

import com.knowave.monomarket.domains.auth.jwt.JwtProvider
import com.knowave.monomarket.domains.user.entity.User
import com.knowave.monomarket.domains.user.repository.UserRepository
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityIntegrationTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository,
) {
    @Test
    fun `social login endpoint is public`() {
        mockMvc.perform(
            post("/api/v1/auth/social-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "provider": "KAKAO",
                      "token": "mock:public-user",
                      "deviceId": "ios-device-1",
                      "deviceName": "iPhone"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken", notNullValue()))
            .andExpect(jsonPath("$.refreshToken", notNullValue()))
            .andExpect(jsonPath("$.isNewUser").value(true))
    }

    @Test
    fun `users me requires authentication`() {
        mockMvc.perform(get("/api/v1/users/me"))
            .andExpect(status().isUnauthorized)
            .andExpect(content().json("""{"message":"Unauthorized"}"""))
    }

    @Test
    fun `users me returns current user with access token`() {
        val user = userRepository.save(
            User(nickname = "test_${UUID.randomUUID().toString().take(8)}")
        )
        val userId = requireNotNull(user.id)
        val accessToken = jwtProvider.generateAccessToken(userId)

        mockMvc.perform(
            get("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId.toString()))
            .andExpect(jsonPath("$.nickname").value(user.nickname))
    }
}
