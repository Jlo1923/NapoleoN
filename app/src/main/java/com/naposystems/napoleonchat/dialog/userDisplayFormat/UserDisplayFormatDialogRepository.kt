package com.naposystems.napoleonchat.dialog.userDisplayFormat

interface UserDisplayFormatDialogRepository {

        fun setUserDisplayFormat(format: Int)
        fun getUserDisplayFormat(): Int

}