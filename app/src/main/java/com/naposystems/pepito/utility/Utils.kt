package com.naposystems.pepito.utility

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.naposystems.pepito.R
import timber.log.Timber
import kotlin.math.roundToInt


class Utils {

    companion object {

        var errorIndex = 0

        fun openKeyboard(textInput: TextInputEditText) {
            val context = textInput.context

            val inputMethodManager = context
                .getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(textInput, SHOW_IMPLICIT)
        }

        /**
         * Convert dp/dpi to pixel values
         * @param context need to get display metrics
         * @param dp DP value
         * @return pixel value
         */
        fun dpToPx(context: Context, dp: Float): Int {
            val density = context.resources.displayMetrics.density
            return (dp * density).roundToInt()
        }

        fun showSimpleSnackbar(
            coordinatorLayout: CoordinatorLayout,
            message: String,
            maxLines: Int
        ) {
            val snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.okay) {
                }
            val snackbarView = snackbar.view

            val textView = snackbarView.findViewById<TextView>(R.id.snackbar_text)
            textView.maxLines = maxLines
            snackbar.show()
        }
    }
}