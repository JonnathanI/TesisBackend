package com.duolingo.clone.language_backend.dto

data class ChallengesResponse(
    val dailyExpProgress: Long,
    val dailyExpGoal: Int = 50,
    val minutesLearned: Int,
    val minutesGoal: Int = 15,
    val perfectLessonsCount: Int,
    val perfectLessonsGoal: Int = 2,
    val challengesCompleted: Int,

    // 👇 reto extra con texto (ya lo tenías)
    val sqlTitle: String,
    val sqlSnippet: String,

    // 👇 LO NUEVO: lista de retos que el front va a pintar
    val challenges: List<SingleChallengeDTO>
)