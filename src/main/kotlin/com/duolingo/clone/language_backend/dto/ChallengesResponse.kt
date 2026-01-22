package com.duolingo.clone.language_backend.dto

data class ChallengesResponse(
    val dailyExpProgress: Long,
    val dailyExpGoal: Int = 50, // Meta fija o configurable
    val minutesLearned: Int,
    val minutesGoal: Int = 15,
    val perfectLessonsCount: Int,
    val perfectLessonsGoal: Int = 2,
    val challengesCompleted: Int
)
