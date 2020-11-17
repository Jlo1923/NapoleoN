package com.naposystems.napoleonchat.model.home

data class Chat(
    val imageUrl: String,
    val fullName: String,
    val nickname: String,
    val message: String,
    val date: String
)