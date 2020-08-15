package com.naposystems.napoleonchat.utility

import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import com.naposystems.napoleonchat.R

class SnackbarUtils(
    private val coordinatorLayout: CoordinatorLayout,
    private val errorList: List<String>
) {

    private var errorIndex = 0
    private var hasFinishedShowingErrors = false
    private lateinit var snackbar: Snackbar

    fun showSnackbar() {
        if (errorList.isNotEmpty()) {
            snackbar =
                Snackbar.make(coordinatorLayout, errorList[errorIndex], Snackbar.LENGTH_INDEFINITE)
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            snackbarErrorHandler()
                        }
                    })
                    .setAction(R.string.text_okay) {
                        // Intentionally empty
                    }
            val snackbarView = snackbar.view

            val textView = snackbarView.findViewById<TextView>(R.id.snackbar_text)
            textView.maxLines = 5

            snackbar.show()
        }
    }

    private fun snackbarErrorHandler() {
        if (errorIndex < errorList.size - 1 && !hasFinishedShowingErrors) {
            errorIndex += 1
            showSnackbar()
        } else {
            errorIndex = 0
            hasFinishedShowingErrors = true
        }
    }
}