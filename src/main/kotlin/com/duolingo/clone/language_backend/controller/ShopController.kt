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
    ): ResponseEntity<Void> {
        val itemType = body["itemType"] ?: return ResponseEntity.badRequest().build()
        val id = UUID.fromString(userId)

        try {
            shopService.buyItem(id, itemType)
            return ResponseEntity.ok().build()
        } catch (e: Exception) {
            return ResponseEntity.badRequest().build()
        }
    }
}