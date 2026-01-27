package com.duolingo.clone.language_backend.repository

import com.duolingo.clone.language_backend.entity.UserRelationship
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRelationshipRepository : JpaRepository<UserRelationship, UUID> {
    fun findByFollowerId(followerId: UUID): List<UserRelationship>

    fun findByFollowerIdAndFollowedId(followerId: UUID, followedId: UUID): UserRelationship?

    fun existsByFollowerIdAndFollowedId(followerId: UUID, followedId: UUID): Boolean

    fun findByFollowedIdAndStatus(followedId: UUID, status: String): List<UserRelationship>
}