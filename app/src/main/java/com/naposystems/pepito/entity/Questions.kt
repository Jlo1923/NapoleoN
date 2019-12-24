package com.naposystems.pepito.entity

data class Questions(
    val id: Int,
    val question: String
) {
    override fun toString(): String {
        return question
    }
}

