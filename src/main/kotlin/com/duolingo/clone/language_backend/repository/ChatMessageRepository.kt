package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.ChatMessageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ChatMessageRepository : JpaRepository<ChatMessageEntity, Long> {

    @Query(
        """
        SELECT m 
        FROM ChatMessageEntity m
        WHERE 
            (m.sender.id = :currentUserId AND m.receiver.id = :friendId)
            OR
            (m.sender.id = :friendId AND m.receiver.id = :currentUserId)
        ORDER BY m.createdAt ASC
        """
    )
    fun findConversation(
        @Param("currentUserId") currentUserId: UUID,
        @Param("friendId") friendId: UUID
    ): List<ChatMessageEntity>
}
