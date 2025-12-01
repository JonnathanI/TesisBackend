package com.duolingo.clone.language_backend.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Excepción lanzada cuando un usuario se queda sin corazones
 * y debe terminar la sesión de práctica.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST) // Usamos 400 Bad Request o 403 Forbidden si quieres ser más estricto
class GameEndException(message: String) : RuntimeException(message)