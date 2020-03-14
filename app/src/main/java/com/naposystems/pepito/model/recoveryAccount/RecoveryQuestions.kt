package com.naposystems.pepito.model.recoveryAccount

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RecoveryQuestions(
    val questionId: Int = 0,
    val question: String = "",
    val answer: List<String> = ArrayList()
): Parcelable