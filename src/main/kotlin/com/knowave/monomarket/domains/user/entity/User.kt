package com.knowave.monomarket.domains.user.entity

import com.knowave.monomarket.common.entity.BaseEntity
import com.knowave.monomarket.common.enum.UserStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, unique = true, length = 50)
    var nickname: String,

    @Column(nullable = true, length = 300)
    var profileImageUrl: String? = null,

    @Column(nullable = false )
    var status: String = UserStatus.ACTIVE.name,
) : BaseEntity()