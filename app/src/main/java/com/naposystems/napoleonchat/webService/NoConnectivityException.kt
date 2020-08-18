package com.naposystems.napoleonchat.webService

import com.naposystems.napoleonchat.R
import java.io.IOException

class NoConnectivityException : IOException() {
    override val message: String?
        get() = R.string.text_error_connection.toString()
}