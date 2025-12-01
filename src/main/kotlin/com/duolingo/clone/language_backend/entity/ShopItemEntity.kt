package com.duolingo.clone.language_backend.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "shop_item")
data class ShopItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = UUID.randomUUID(),

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val description: String,

    @Column(nullable = false)
    val price: Int, // Costo en Lingots

    @Column(nullable = false)
    val itemType: String // Ej: "HEART_REFILL", "STREAK_FREEZE"
)