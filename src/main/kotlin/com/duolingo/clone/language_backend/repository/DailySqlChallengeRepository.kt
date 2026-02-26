package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.DailySqlChallengeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DailySqlChallengeRepository : JpaRepository<DailySqlChallengeEntity, Long>