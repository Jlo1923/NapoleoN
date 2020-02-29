package com.naposystems.pepito.webService

import com.naposystems.pepito.R
import java.io.IOException

class NoConnectivityException : IOException() {
    override val message: String?
        get() = R.string.text_error_connection.toString()
}