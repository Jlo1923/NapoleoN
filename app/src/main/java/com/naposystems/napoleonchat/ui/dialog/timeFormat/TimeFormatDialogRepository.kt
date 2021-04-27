package com.naposystems.napoleonchat.ui.dialog.timeFormat

interface TimeFormatDialogRepository {
    fun setTimeFormat(format: Int)
    fun getTimeFormat(): Int
}