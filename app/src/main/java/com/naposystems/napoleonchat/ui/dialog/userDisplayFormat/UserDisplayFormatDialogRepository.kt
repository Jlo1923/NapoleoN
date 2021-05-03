package com.naposystems.napoleonchat.ui.dialog.userDisplayFormat

interface UserDisplayFormatDialogRepository {

        fun setUserDisplayFormat(format: Int)
        fun getUserDisplayFormat(): Int

}