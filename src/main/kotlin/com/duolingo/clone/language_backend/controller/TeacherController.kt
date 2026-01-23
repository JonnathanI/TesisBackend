package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.StudentDataDTO
import com.duolingo.clone.language_backend.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/teacher") // <--- RUTA BASE: /api/teacher
class TeacherController(private val userService: UserService) {

    // Endpoint: GET /api/teacher/students
    @GetMapping("/students") // <--- RUTA COMPLETA: /api/teacher/students
    fun getAllStudents(): ResponseEntity<List<StudentDataDTO>> {
        val students = userService.getAllStudents()
        return ResponseEntity.ok(students)
    }

    @PostMapping("/generate-classroom-code")
    fun generateCode(
        @AuthenticationPrincipal principal: String
    ): ResponseEntity<Map<String, String>> {

        val teacherUuid = UUID.fromString(principal)

        val code = userService.generateRegistrationCode(teacherUuid)

        return ResponseEntity.ok(mapOf("code" to code))

    }


}