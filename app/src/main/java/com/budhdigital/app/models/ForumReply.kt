package com.budhdigital.app.models

data class ForumReply(
    val id: String = "",
    val questionId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val reply: String = "",
    val timestamp: Long = 0,
    val likeCount: Int = 0,
    val likedBy: Map<String, Boolean> = emptyMap()
) {
    constructor() : this("", "", "", "", "", "", 0, 0, emptyMap())
}

