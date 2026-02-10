package com.duolingo.clone.language_backend.controller

import com.duolingo.clone.language_backend.dto.LeaderboardEntryDTO
import com.duolingo.clone.language_backend.dto.UnitStatusDTO
import com.duolingo.clone.language_backend.service.ClassroomService
import com.duolingo.clone.language_backend.service.ProgressService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/student/classrooms")
class StudentClassroomController(
    private val classroomService: ClassroomService,
    private val progressService: ProgressService
) {

    // UNIRSE A CLASE
    @PostMapping("/join")
    fun joinClassroom(
        @AuthenticationPrincipal userId: String,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<Void> {
        val studentId = UUID.fromString(userId)
        val code = request["code"] ?: return ResponseEntity.badRequest().build()

        try {
            classroomService.joinClassroom(studentId, code)
            return ResponseEntity.ok().build()
        } catch (e: Exception) {
            return ResponseEntity.badRequest().build()
        }
    }

    // LISTAR MIS CLASES
    @GetMapping
    fun getMyClassrooms(@AuthenticationPrincipal userId: String): ResponseEntity<List<Map<String, Any>>> {
        val studentId = UUID.fromString(userId)
        val classrooms = classroomService.getStudentClassrooms(studentId)

        val response = classrooms.map { c ->
            mapOf(
                "id" to c.id!!,
                "name" to c.name,
                "teacherName" to c.teacher.fullName
            )
        }
        return ResponseEntity.ok(response)
    }

    // VER DETALLE DE CLASE (TAREAS)
    @GetMapping("/{id}")
    fun getClassroomDetails(
        @AuthenticationPrincipal userId: String,
        @PathVariable id: UUID
    ): ResponseEntity<Map<String, Any>> {
        val studentId = UUID.fromString(userId)

        try {
            val classroom = classroomService.getStudentClassroomDetails(studentId, id)

            val response = mapOf(
                "id" to classroom.id!!,
                "name" to classroom.name,
                "teacherName" to classroom.teacher.fullName,
                "assignments" to classroom.assignments.map { a ->
                    mapOf(
                        "id" to a.id!!,
                        "title" to a.title,
                        "description" to a.description,
                        "xpReward" to a.xpReward,
                        "dueDate" to (a.dueDate?.toString() ?: "Sin fecha")
                    )
                }
            )
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            return ResponseEntity.status(403).build()
        }
    }

    // --- ¡ESTE ES EL ENDPOINT QUE TE FALTA! (RANKING) ---
    @GetMapping("/{id}/leaderboard")
    fun getClassroomLeaderboard(
        @AuthenticationPrincipal userId: String,
        @PathVariable id: UUID
    ): ResponseEntity<List<LeaderboardEntryDTO>> {
        val studentId = UUID.fromString(userId)

        // 1. Obtenemos la clase (validando que el alumno pertenezca a ella)
        val classroom = classroomService.getStudentClassroomDetails(studentId, id)

        // 2. Ordenamos a los estudiantes por XP y creamos el ranking
        val leaderboard = classroom.students
            .sortedByDescending { it.xpTotal } // Ordenar de mayor a menor
            .mapIndexed { index, student ->
                LeaderboardEntryDTO(
                    userId = student.id!!,
                    fullName = student.fullName,
                    xpTotal = (student.xpTotal ?: 0L).toInt(),
                            position = index + 1
                )
            }

        return ResponseEntity.ok(leaderboard)
    }

    @GetMapping("/{id}/units")
    fun getClassroomUnits(
        @AuthenticationPrincipal userId: String,
        @PathVariable id: UUID
    ): ResponseEntity<List<UnitStatusDTO>> {
        val studentId = UUID.fromString(userId)

        // Verificamos que el alumno pertenezca a la clase
        val classroom = classroomService.getStudentClassroomDetails(studentId, id)

        // 👇 IMPORTANTE: que Classroom tenga un course asociado
        val courseId = classroom.course.id
            ?: throw RuntimeException("La clase no tiene curso asignado")

        val units = progressService.getCourseProgress(courseId, studentId)
        return ResponseEntity.ok(units)
    }
}