package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.TransactionLog // <-- Importa el nombre correcto
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

// Corrige el nombre de la Entidad de TransactionLogEntity a TransactionLog
interface TransactionLogRepository : JpaRepository<TransactionLog, UUID>