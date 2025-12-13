package com.duolingo.clone.language_backend.service

import com.duolingo.clone.language_backend.entity.AssignmentEntity
import com.duolingo.clone.language_backend.entity.ClassroomEntity
import com.duolingo.clone.language_backend.repository.AssignmentRepository
import com.duolingo.clone.language_backend.repository.ClassroomRepository
import com.duolingo.clone.language_backend.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID
import kotlin.random.Random

@Service
class ClassroomService(
    private val classroomRepository: ClassroomRepository,
    private val userRepository: UserRepository,
    private val assignmentRepository: AssignmentRepository
) {

    private fun generateUniqueCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val nums = "0123456789"
        var code = ""
        do {
            val part1 = (1..3).map { chars[Random.nextInt(chars.length)] }.joinToString("")
            val part2 = (1..3).map { nums[Random.nextInt(nums.length)] }.joinToString("")
            code = "$part1-$part2"
        } while (classroomRepository.existsByCode(code))
        return code
    }

    fun createClassroom(teacherId: UUID, name: String): ClassroomEntity {
        val teacher = userRepository.findById(teacherId)
            .orElseThrow { RuntimeException("Profesor no encontrado") }

        val newClass = ClassroomEntity(
            name = name,
            code = generateUniqueCode(),
            teacher = teacher
        )
        return classroomRepository.save(newClass)
    }

    fun getTeacherClassrooms(teacherId: UUID): List<ClassroomEntity> {
        return classroomRepository.findAllByTeacherId(teacherId)
    }

    fun deleteClassroom(classId: UUID) {
        classroomRepository.deleteById(classId)
    }

    fun joinClassroom(studentId: UUID, code: String) {
        val student = userRepository.findById(studentId)
            .orElseThrow { RuntimeException("Estudiante no encontrado") }

        val classroom = classroomRepository.findByCode(code)
            ?: throw RuntimeException("Código de clase inválido")

        if (!classroom.students.contains(student)) {
            classroom.students.add(student)
            classroomRepository.save(classroom)
        }
    }

    // --- ¡ESTA ES LA FUNCIÓN QUE FALTABA PARA EL ERROR DEL CONTROLADOR! ---
    fun getStudentClassrooms(studentId: UUID): List<ClassroomEntity> {
        return classroomRepository.findAllByStudentsId(studentId)
    }
    // ---------------------------------------------------------------------

    fun addStudentByEmail(classroomId: UUID, studentEmail: String) {
        val classroom = classroomRepository.findById(classroomId)
            .orElseThrow { RuntimeException("Clase no encontrada") }

        // CORRECCIÓN: Usamos el operador Elvis (?:) porque findByEmail puede devolver null
        val student = userRepository.findByEmail(studentEmail)
            ?: throw RuntimeException("No existe un usuario con el email: $studentEmail")

        if (!classroom.students.contains(student)) {
            classroom.students.add(student)
            classroomRepository.save(classroom)
        }
    }

    fun createAssignment(classroomId: UUID, title: String, desc: String, xp: Int, date: LocalDate?): AssignmentEntity {
        val classroom = classroomRepository.findById(classroomId)
            .orElseThrow { RuntimeException("Clase no encontrada") }

        val assignment = AssignmentEntity(
            title = title,
            description = desc,
            xpReward = xp,
            dueDate = date,
            classroom = classroom
        )
        return assignmentRepository.save(assignment)
    }

    fun getAssignments(classroomId: UUID): List<AssignmentEntity> {
        return assignmentRepository.findAllByClassroomIdOrderByDueDateDesc(classroomId)
    }

    fun getClassroomDetails(classroomId: UUID): ClassroomEntity {
        val classroom = classroomRepository.findById(classroomId)
            .orElseThrow { RuntimeException("Clase no encontrada") }
        classroom.students.size
        return classroom
    }

    fun getStudentClassroomDetails(studentId: UUID, classroomId: UUID): ClassroomEntity {
        val classroom = classroomRepository.findById(classroomId)
            .orElseThrow { RuntimeException("Clase no encontrada") }

        val isEnrolled = classroom.students.any { it.id == studentId }
        if (!isEnrolled) {
            throw RuntimeException("No tienes permiso para ver esta clase.")
        }
        classroom.assignments.size
        return classroom
    }
}