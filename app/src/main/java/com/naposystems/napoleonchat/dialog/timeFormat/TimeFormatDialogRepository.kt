package com.naposystems.napoleonchat.dialog.timeFormat

interface TimeFormatDialogRepository {
    fun setTimeFormat(format: Int)
    fun getTimeFormat(): Int
}