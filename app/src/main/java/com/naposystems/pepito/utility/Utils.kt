package com.naposystems.pepito.utility

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
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
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import com.google.android.material.snackbar.Snackbar
import com.naposystems.pepito.R
import com.naposystems.pepito.ui.generalDialog.GeneralDialogFragment
import com.naposystems.pepito.utility.dialog.PermissionDialogFragment
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.text.SimpleDateFormat
import kotlin.math.roundToInt

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

        fun convertFileInputStreamToBase64(fileInputStream: FileInputStream) =
            fileInputStream.use { inputStream ->
                ByteArrayOutputStream().use { outPutStream ->
                    Base64OutputStream(outPutStream, Base64.DEFAULT).use { base64FileStream ->
                        inputStream.copyTo(base64FileStream)
                        base64FileStream.close()
                        outPutStream.toString()
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


        fun convertBooleanToInvertedInt(boolean: Boolean): Int {
            return if (boolean) {
                0
            } else {
                1
            }
        }

        @SuppressLint("SimpleDateFormat")
        fun dateToTimestamp(day: Int, month: Int, year: Int): Long =
            SimpleDateFormat("dd.MM.yyyy").let { formatter ->
                formatter.parse("$day.$month.$year")?.time ?: 0
            }

        fun getDuration(millisUntilFinished: Long, showHours: Boolean = true): String {
            var duration = ""
            val hour = ((millisUntilFinished / 1000) / 60) / 60
            val minutes = ((millisUntilFinished / 1000) / 60) % 60
            val seconds = (millisUntilFinished / 1000) % 60

            if (showHours) {
                duration += if (hour < 10) "0${hour}:" else "$hour:"
            } else if (hour > 0) {
                duration += if (hour < 10) "0${hour}:" else "$hour:"
            }

            duration += if (minutes < 10) "0${minutes}:" else "$minutes:"
            duration += if (seconds < 10) "0${seconds}" else "$seconds"

            return duration
        }

        fun getFileSize(sizeInByte: Long): String {
            val kilobyte = sizeInByte / 1000
            val megabyte = ((kilobyte / 1000f) * 10.0).roundToInt() / 10.0
            val gigabyte = ((megabyte / 1000) * 10.0).roundToInt() / 10.0

            return when {
                gigabyte > 1 -> "$gigabyte GB"
                megabyte > 1 -> "$megabyte MB"
                else -> "$kilobyte kB"
            }
        }

        fun copyEncryptedFile(
            context: Context,
            fileInputStream: FileInputStream,
            subFolder: String,
            fileName: String
        ): File {
            val path = File(context.externalCacheDir!!, subFolder)
            if (!path.exists())
                path.mkdirs()
            val audioFile = File(path, fileName)

            val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
            val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

            val encryptedFile = EncryptedFile.Builder(
                audioFile,
                context,
                masterKeyAlias,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            encryptedFile.openFileOutput().use { fileOut ->
                fileInputStream.copyTo(fileOut)
                fileOut.flush()
                fileOut.close()
            }

            fileInputStream.close()

            return audioFile
        }

        fun getEncryptedFile(context: Context, file: File): EncryptedFile {
            val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
            val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

            return EncryptedFile.Builder(
                file,
                context,
                masterKeyAlias,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
        }

        fun decodeFileDescriptorToBitmap(fileDescriptor: FileDescriptor): Bitmap? {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            return try {
                BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
            } catch (e: Exception) {
                Timber.e(e)
                null
            }
        }
    }
}