package com.naposystems.pepito.model.recoveryAccount

data class RecoveryQuestions(
    val questionId: Int,
    val question: String,
    val answer: List<String>
)