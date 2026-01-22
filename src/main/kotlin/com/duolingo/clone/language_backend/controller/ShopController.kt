package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.service.ShopService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/shop")
class ShopController(private val shopService: ShopService) {

    @PostMapping("/buy")
    fun buyItem(
        @AuthenticationPrincipal userId: String,
        @RequestBody body: Map<String, String>
    ): ResponseEntity<Any> { // Cambiado a Any para enviar mensajes
        val itemType = body["itemType"] ?: return ResponseEntity.badRequest().body("Falta itemType")

        return try {
            val id = UUID.fromString(userId)
            shopService.buyItem(id, itemType)
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            // Esto te dirá en la consola del navegador cuál fue el error real
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}