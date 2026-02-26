package com.duolingo.clone.language_backend.dto

data class DailyStats(
    val dailyXp: Int,
    val dailyMinutes: Int,
    val perfectLessons: Int,
    val lessonsToday: Int,
    val unitsPracticedToday: Int,
    val currentStreak: Int,
    val lingotsGainedToday: Int,
    val heartsCount: Int,
    val correctAnswersToday: Int
)