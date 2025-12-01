package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.PurchaseRequest
import com.duolingo.clone.language_backend.entity.UserEntity
import com.duolingo.clone.language_backend.service.ShopService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/shop")
class ShopController(
    private val shopService: ShopService
) {
    /**
     * POST /api/shop/buy
     * Permite a un usuario autenticado comprar un ítem de la tienda
     * (Corazones o Congelador de Racha).
     */
    @PostMapping("/buy")
    fun buyItem(
        @AuthenticationPrincipal userId: String,
        @RequestBody request: PurchaseRequest
    ): ResponseEntity<UserEntity> {

        val userUuid = UUID.fromString(userId)

        // El ShopService maneja la validación de fondos y aplica el efecto
        val updatedUser = shopService.buyItem(userUuid, request.itemType)

        return ResponseEntity.ok(updatedUser)
    }

    // Opcional: Endpoint para listar precios si no se asumen estáticos
    /*
    @GetMapping("/items")
    fun listItems(): ResponseEntity<Map<String, Int>> {
        // Podrías devolver los precios fijos desde el servicio
        val items = mapOf(
            "HEART_REFILL" to 450,
            "STREAK_FREEZE" to 200
        )
        return ResponseEntity.ok(items)
    }
    */
}