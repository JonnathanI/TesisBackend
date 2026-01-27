package com.duolingo.clone.language_backend.dto

import com.duolingo.clone.language_backend.enums.Role // Importa tu Enum Role

data class BulkRegisterRequest(
    val users: List<BulkUserItem>,    // Cambiamos 'students' por 'users' (más genérico)
    val registrationCode: String,     // Código de aula o código de administrador
    val roleToAssign: Role            // 👈 NUEVO: STUDENT o TEACHER
)