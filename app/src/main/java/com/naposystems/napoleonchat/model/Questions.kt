package com.naposystems.napoleonchat.model

data class Questions(
    val id: Int,
    val question: String
) {
    override fun toString(): String {
        return question
    }
}

