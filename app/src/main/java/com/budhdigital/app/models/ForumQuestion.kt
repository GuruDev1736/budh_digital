package com.budhdigital.app.models

data class ForumQuestion(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val question: String = "",
    val description: String = "",
    val timestamp: Long = 0,
    val replyCount: Int = 0,
    val likeCount: Int = 0,
    val dislikeCount: Int = 0,
    val likedBy: Map<String, Boolean> = emptyMap(),
    val dislikedBy: Map<String, Boolean> = emptyMap()
) {
    constructor() : this("", "", "", "", "", "", 0, 0, 0, 0, emptyMap(), emptyMap())
}



