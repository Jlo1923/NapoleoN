package com.naposystems.pepito.utility

import android.content.ContentResolver
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import android.util.Base64
import android.util.Base64OutputStream
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import com.naposystems.pepito.R
import com.naposystems.pepito.ui.generalDialog.GeneralDialogFragment
import com.naposystems.pepito.utility.dialog.PermissionDialogFragment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import kotlin.math.roundToInt
import android.graphics.BitmapFactory
import android.provider.OpenableColumns
import android.view.View

class Utils {

    companion object {

        fun openKeyboard(view: View) {
            val context = view.context

            val inputMethodManager = context
                .getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(view, SHOW_IMPLICIT)
        }

        fun hideKeyboard(view: View) {

            val context = view.context

            val inputManager: InputMethodManager =
                context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                view.windowToken,
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

        fun showDialogToInformPermission(
            context: Context,
            fragmentManager: FragmentManager,
            icon: Int,
            message: Int,
            accept: () -> Unit,
            cancel: () -> Unit
        ) {

            val dialog = PermissionDialogFragment.newInstance(
                icon,
                context.resources.getString(message)
            )
            dialog.setListener(object : PermissionDialogFragment.OnDialogListener {
                override fun onAcceptPressed() {
                    accept()
                }

                override fun onCancelPressed() {
                    cancel()
                }
            })
            dialog.show(fragmentManager, "Test")
        }

        fun openSetting(context: Context) {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        fun getCacheImagePath(context: Context, fileName: String, subFolder: String): Uri {
            val path = File(context.externalCacheDir!!, subFolder)
            if (!path.exists())
                path.mkdirs()
            val image = File(path, fileName)
            return FileProvider.getUriForFile(context, "com.naposystems.pepito.provider", image)
        }

        fun convertBitmapToBase64(bitmap: Bitmap): String {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        }

        fun base64ToBitmap(b64: String): Bitmap {
            val imageAsBytes = Base64.decode(b64.toByteArray(), Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
        }

        fun convertImageFileToBase64(imageFile: File): String {
            return FileInputStream(imageFile).use { inputStream ->
                ByteArrayOutputStream().use { outputStream ->
                    Base64OutputStream(outputStream, Base64.DEFAULT).use { base64FilterStream ->
                        inputStream.copyTo(base64FilterStream)
                        base64FilterStream.close()
                        outputStream.toString()
                    }
                }
            }
        }

        fun generalDialog(
            title: String,
            message: String,
            isCancelable: Boolean,
            childFragmentManager: FragmentManager,
            actionAccept: () -> Unit
        ) {
            val dialog = GeneralDialogFragment.newInstance(title, message, isCancelable)
            dialog.setListener(object : GeneralDialogFragment.OnGeneralDialog {
                override fun onAccept() {
                    actionAccept()
                }
            })
            dialog.show(childFragmentManager, "GeneralDialog")
        }

        fun queryName(resolver: ContentResolver, uri: Uri): String {
            val returnCursor =
                resolver.query(uri, null, null, null, null)
            assert(returnCursor != null)
            val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            val name = returnCursor.getString(nameIndex)
            returnCursor.close()
            return name
        }


        fun convertBooleanToInvertedInt(boolean: Boolean) : Int {
            return if(boolean) {
                0
            } else {
                1
            }
        }
    }
}