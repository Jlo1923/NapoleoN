package com.naposystems.pepito.utility

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Bitmap
import android.util.Base64
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.naposystems.pepito.R
import com.naposystems.pepito.ui.generalDialog.GeneralDialogFragment
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt


class Utils {

    companion object {

        fun openKeyboard(textInput: TextInputEditText) {
            val context = textInput.context

            val inputMethodManager = context
                .getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(textInput, SHOW_IMPLICIT)
        }

        fun hideKeyboard(textInput: TextInputEditText) {

            val context = textInput.context

            val inputManager: InputMethodManager =
                context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                textInput.windowToken,
                InputMethodManager.SHOW_FORCED
            )
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

        fun convertBitmapToBase64(bitmap: Bitmap): String? {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        }

        fun generalDialog(
            title: String,
            message: String,
            childFragmentManager: FragmentManager,
            actionAccept: () -> Unit
        ) {
            val dialog = GeneralDialogFragment.newInstance(title, message)
            dialog.setListener(object : GeneralDialogFragment.OnGeneralDialog {
                override fun onAccept() {
                    actionAccept()
                }
            })
            dialog.show(childFragmentManager, "GeneralDialog")
        }
    }
}