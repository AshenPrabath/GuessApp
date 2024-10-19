package com.example.madguessapp.model

data class LeaderboardResponse(
    val dreamlo: Dreamlo
)
data class Dreamlo(
    val leaderboard: Leaderboard
)

data class Leaderboard(
    val entry: List<Entry>
)

data class Entry(
    val name: String,
    val score: String,
    val seconds: String,
    val text: String,
    val date: String
)