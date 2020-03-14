package com.naposystems.pepito.model.recoveryAccount

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RecoveryAccountUserType(
    val userType: Int = 0,
    val newRecoveryInfo: List<RecoveryQuestions> = ArrayList()
): Parcelable