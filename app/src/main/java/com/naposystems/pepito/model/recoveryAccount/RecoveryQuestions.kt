package com.naposystems.pepito.model.recoveryAccount

data class RecoveryQuestions(
    val questionId: Int = 0,
    val question: String = "",
    val answer: List<String> = ArrayList()
)