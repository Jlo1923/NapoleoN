package com.naposystems.pepito.model.recoveryOlderAccount

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RecoveryOlderAccountQuestions (
    val firstQuestionId: Int,
    val firstQuestion: String,
    val secondQuestionId: Int,
    val secondQuestion: String
): Parcelable