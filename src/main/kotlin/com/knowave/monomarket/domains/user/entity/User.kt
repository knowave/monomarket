package com.knowave.monomarket.domains.user.entity

import com.knowave.monomarket.common.entity.BaseEntity
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
    var status: String = "ACTIVE",
) : BaseEntity()