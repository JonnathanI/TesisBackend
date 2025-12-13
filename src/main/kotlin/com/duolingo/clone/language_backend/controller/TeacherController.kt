package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.StudentDataDTO
import com.duolingo.clone.language_backend.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/teacher") // <--- RUTA BASE: /api/teacher
class TeacherController(private val userService: UserService) {

    // Endpoint: GET /api/teacher/students
    @GetMapping("/students") // <--- RUTA COMPLETA: /api/teacher/students
    fun getAllStudents(): ResponseEntity<List<StudentDataDTO>> {
        val students = userService.getAllStudents()
        return ResponseEntity.ok(students)
    }
}