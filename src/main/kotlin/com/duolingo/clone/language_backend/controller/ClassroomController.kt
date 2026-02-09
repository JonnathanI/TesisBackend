package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.ClassroomDetailDTO
import com.duolingo.clone.language_backend.dto.StudentSummaryDTO
import com.duolingo.clone.language_backend.entity.AssignmentEntity
import com.duolingo.clone.language_backend.entity.ClassroomEntity
import com.duolingo.clone.language_backend.service.ClassroomService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/teacher/classrooms")
class ClassroomController(private val classroomService: ClassroomService) {

    @GetMapping
    fun getMyClassrooms(@AuthenticationPrincipal userId: String): ResponseEntity<List<ClassroomEntity>> {
        val teacherId = UUID.fromString(userId)
        return ResponseEntity.ok(classroomService.getTeacherClassrooms(teacherId))
    }

    @PostMapping
    fun createClassroom(
        @AuthenticationPrincipal userId: String,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<ClassroomEntity> {
        val teacherId = UUID.fromString(userId)
        val name = request["name"] ?: return ResponseEntity.badRequest().build()

        val newClass = classroomService.createClassroom(teacherId, name)
        return ResponseEntity.ok(newClass)
    }

    @DeleteMapping("/{id}")
    fun deleteClassroom(@PathVariable id: UUID): ResponseEntity<Void> {
        classroomService.deleteClassroom(id)
        return ResponseEntity.ok().build()
    }

    // --- CORRECCIÓN AQUÍ ---
    @GetMapping("/{id}")
    fun getClassroomDetails(@PathVariable id: UUID): ResponseEntity<ClassroomDetailDTO> {
        val classroom = classroomService.getClassroomDetails(id)

        val responseDTO = ClassroomDetailDTO(
            id = classroom.id!!,
            name = classroom.name,
            code = classroom.code,
            students = classroom.students.map { student ->
                StudentSummaryDTO(
                    id = student.id!!,
                    fullName = student.fullName,
                    email = student.email,
                    // SOLUCIÓN: Agregamos .toLong() para convertir el Int a Long
                    xpTotal = student.xpTotal.toLong(),
                    currentStreak = student.currentStreak,
                )
            }
        )

        return ResponseEntity.ok(responseDTO)
    }
    // -----------------------

    @PostMapping("/{id}/students")
    fun addStudent(@PathVariable id: UUID, @RequestBody body: Map<String, String>): ResponseEntity<Void> {
        val email = body["email"] ?: return ResponseEntity.badRequest().build()
        classroomService.addStudentByEmail(id, email)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{id}/assignments")
    fun createAssignment(
        @PathVariable id: UUID,
        @RequestBody body: Map<String, Any>
    ): ResponseEntity<AssignmentEntity> {
        val title = body["title"] as String
        val description = body["description"] as String

        // Es más seguro castear a Number y luego a Int para evitar errores de JSON
        val xp = (body["xp"] as Number).toInt()

        val dateStr = body["dueDate"] as String?
        val date = if (dateStr != null) LocalDate.parse(dateStr) else null

        val assignment = classroomService.createAssignment(id, title, description, xp, date)
        return ResponseEntity.ok(assignment)
    }

    @GetMapping("/{id}/assignments")
    fun getAssignments(@PathVariable id: UUID): ResponseEntity<List<AssignmentEntity>> {
        return ResponseEntity.ok(classroomService.getAssignments(id))
    }
}