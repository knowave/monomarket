package com.knowave.monomarket.domains.user.repository

import com.knowave.monomarket.domains.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {
    fun existsByIdAndStatus(
        id: UUID,
        status: String,
    ): Boolean
}
