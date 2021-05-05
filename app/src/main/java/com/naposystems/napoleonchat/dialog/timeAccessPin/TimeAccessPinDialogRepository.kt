package com.naposystems.napoleonchat.dialog.timeAccessPin

interface TimeAccessPinDialogRepository {
    suspend fun getTimeAccessPin(): Int
    suspend fun setTimeAccessPin(time: Int)
    suspend fun setLockType(type: Int)
}