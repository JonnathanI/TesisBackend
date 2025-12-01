package com.duolingo.clone.language_backend.entity

import java.io.Serializable
import java.util.UUID

// NO es una data class, sino una clase normal para tener control del constructor.
class UserLessonProgressId(
    // Propiedades mutables (var) y nulas por defecto
    var user: UUID? = null,
    var lesson: UUID? = null
) : Serializable {

    // ¡IMPORTANTE! Constructor secundario sin argumentos (requerido por Hibernate)
    // Inicializa las propiedades con los valores por defecto.
    constructor() : this(null, null)

    // Los métodos equals y hashCode son esenciales para JPA/Hibernate
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserLessonProgressId

        if (user != other.user) return false
        if (lesson != other.lesson) return false

        return true
    }

    override fun hashCode(): Int {
        var result = user?.hashCode() ?: 0
        result = 31 * result + (lesson?.hashCode() ?: 0)
        return result
    }
}