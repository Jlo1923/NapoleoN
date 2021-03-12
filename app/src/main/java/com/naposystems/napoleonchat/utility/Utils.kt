package com.naposystems.napoleonchat.utility

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Base64
import android.util.Base64OutputStream
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import com.google.android.material.snackbar.Snackbar
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.service.notification.OLD_NotificationService
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.ui.generalDialog.GeneralDialogFragment
import com.naposystems.napoleonchat.utility.Constants.SelfDestructTime.*
import com.naposystems.napoleonchat.utility.dialog.PermissionDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class Utils {

    companion object {

        private val mediaPlayer: MediaPlayer by lazy {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
        }

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
            inputManager.hideSoftInputFromWindow(view.windowToken, 0)
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
                .setAction(R.string.text_okay) {
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

        fun isInternetAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }

        fun isOnline(): Boolean {
            val runtime = Runtime.getRuntime()
            try {
                val ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
                val exitValue = ipProcess.waitFor()
                return exitValue == 0
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            return false
        }

        fun vibratePhone(context: Context?, effect: Int, duration: Long) {
            val vibrator = context?.let {
                it.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= 26) {
                    when (effect) {
                        Constants.Vibrate.SOFT.type ->
                            vibrator.vibrate(
                                VibrationEffect.createOneShot(
                                    20,
                                    VibrationEffect.EFFECT_TICK
                                )
                            )
                        else ->
                            vibrator.vibrate(
                                VibrationEffect.createOneShot(
                                    duration,
                                    VibrationEffect.DEFAULT_AMPLITUDE
                                )
                            )
                    }
                } else {
                    if (effect == Constants.Vibrate.DEFAULT.type)
                        vibrator.vibrate(duration)
                }
            }
        }

        fun getFileUri(context: Context, fileName: String, subFolder: String): Uri {
            return try {
                val path = File(context.cacheDir!!, subFolder)
                if (!path.exists())
                    path.mkdirs()
                val image = File(path, fileName)
                FileProvider.getUriForFile(context, "com.naposystems.napoleonchat.provider", image)
            } catch (e: Exception) {
                Timber.e(e)
                Uri.parse("")
            }
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
            textButtonAccept: String = "",
            textButtonCancel: String = "",
            actionAccept: () -> Unit
        ) {
            val dialog = GeneralDialogFragment.newInstance(
                title,
                message,
                isCancelable,
                textButtonAccept,
                textButtonCancel
            )
            dialog.setListener(object : GeneralDialogFragment.OnGeneralDialog {
                override fun onAccept() {
                    actionAccept()
                }
            })
            dialog.show(childFragmentManager, "GeneralDialog")
        }

        fun alertDialogWithNeutralButton(
            message: Int,
            isCancelable: Boolean,
            childFragmentManager: Context,
            titleTopButton: Int,
            titleCentralButton: Int,
            titleDownButton: Int,
            clickTopButton: (Boolean) -> Unit,
            clickDownButton: (Boolean) -> Unit
        ) {
            val dialog = AlertDialog.Builder(childFragmentManager, R.style.MyDialogTheme)
                .setMessage(message)
                .setCancelable(isCancelable)
                .setPositiveButton(titleTopButton) { _, _ ->
                    clickTopButton(true)
                }
                .setNeutralButton(titleCentralButton) { dialog, _ ->
                    dialog.dismiss()
                }
                .setNegativeButton(titleDownButton) { _, _ ->
                    clickDownButton(true)
                }
                .create()

            dialog.show()

            val textColorButton =
                convertAttrToColorResource(childFragmentManager, R.attr.attrTextColorButtonTint)

            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(textColorButton)
            positiveButton.setBackgroundColor(Color.TRANSPARENT)
            positiveButton.isAllCaps = false

            val neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            neutralButton.setTextColor(textColorButton)
            neutralButton.setBackgroundColor(Color.TRANSPARENT)
            neutralButton.isAllCaps = false

            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.setTextColor(textColorButton)
            negativeButton.setBackgroundColor(Color.TRANSPARENT)
            negativeButton.isAllCaps = false
        }

        fun alertDialogWithoutNeutralButton(
            message: Int,
            isCancelable: Boolean,
            childFragmentManager: Context,
            location: Int,
            titlePositiveButton: Int,
            titleNegativeButton: Int,
            clickPositiveButton: (Boolean) -> Unit,
            clickNegativeButton: (Boolean) -> Unit
        ) {
            val dialog = AlertDialog.Builder(childFragmentManager, R.style.MyDialogTheme)
                .setMessage(message)
                .setCancelable(isCancelable)
                .setPositiveButton(titlePositiveButton) { _, _ ->
                    clickPositiveButton(true)
                }
                .setNegativeButton(titleNegativeButton) { dialog, _ ->
                    if (location == Constants.LocationAlertDialog.CONVERSATION.location)
                        dialog.dismiss()
                    else {
                        clickNegativeButton(true)
                        dialog.dismiss()
                    }
                }
                .create()
            dialog.show()

            val textColorButton =
                convertAttrToColorResource(childFragmentManager, R.attr.attrTextColorButtonTint)

            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(textColorButton)
            positiveButton.setBackgroundColor(Color.TRANSPARENT)
            positiveButton.isAllCaps = false

            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.setTextColor(textColorButton)
            negativeButton.setBackgroundColor(Color.TRANSPARENT)
            negativeButton.isAllCaps = false
        }

        fun alertDialogInformative(
            title: String,
            message: String,
            isCancelable: Boolean,
            childFragmentManager: Context,
            titleButton: Int,
            clickTopButton: (Boolean) -> Unit
        ) {
            val dialog = AlertDialog.Builder(childFragmentManager, R.style.MyDialogTheme)
                .setMessage(message)
                .setCancelable(isCancelable)
                .setPositiveButton(titleButton) { _, _ ->
                    clickTopButton(true)
                }
                .create()

            if (title.isNotEmpty()) {
                dialog.setTitle(title)
            }

            dialog.show()

            val textColorButton =
                convertAttrToColorResource(childFragmentManager, R.attr.attrTextColorButtonTint)

            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(textColorButton)
            positiveButton.setBackgroundColor(Color.TRANSPARENT)
            positiveButton.isAllCaps = false
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
            return if (boolean) 0 else 1
        }

        fun convertIntToInvertedBoolean(int: Int): Boolean {
            return int == 1
        }

        fun convertAttrToColorResource(context: Context, attr: Int): Int {
            val valueColor = TypedValue()
            context.theme.resolveAttribute(
                attr,
                valueColor,
                true
            )
            return ContextCompat.getColor(context, valueColor.resourceId)
        }

        fun convertItemOfTimeInSeconds(item: Int): Int {
            return when (item) {
                EVERY_FIVE_SECONDS.time -> TimeUnit.SECONDS.toSeconds(5).toInt()
                EVERY_FIFTEEN_SECONDS.time -> TimeUnit.SECONDS.toSeconds(15).toInt()
                EVERY_THIRTY_SECONDS.time -> TimeUnit.SECONDS.toSeconds(30).toInt()
                EVERY_ONE_MINUTE.time -> TimeUnit.MINUTES.toSeconds(1).toInt()
                EVERY_TEN_MINUTES.time -> TimeUnit.MINUTES.toSeconds(10).toInt()
                EVERY_THIRTY_MINUTES.time -> TimeUnit.MINUTES.toSeconds(30).toInt()
                EVERY_ONE_HOUR.time -> TimeUnit.HOURS.toSeconds(1).toInt()
                EVERY_TWELVE_HOURS.time, EVERY_TWENTY_FOUR_HOURS_ERROR.time ->
                    TimeUnit.HOURS.toSeconds(12).toInt()
                EVERY_ONE_DAY.time -> TimeUnit.DAYS.toSeconds(1).toInt()
                EVERY_TWENTY_FOUR_HOURS_ERROR.time -> TimeUnit.DAYS.toSeconds(1).toInt()
                else -> TimeUnit.DAYS.toSeconds(7).toInt()
            }
        }

        fun compareDurationAttachmentWithSelfAutoDestructionInSeconds(
            duration: Int,
            timeActual: Int
        ): Int {
            return if (duration >= convertItemOfTimeInSeconds(timeActual)) {
                when {
                    duration >= TimeUnit.DAYS.toSeconds(1).toInt() -> EVERY_SEVEN_DAY.time
                    duration >= TimeUnit.HOURS.toSeconds(12).toInt() -> EVERY_ONE_DAY.time
                    duration >= TimeUnit.HOURS.toSeconds(1).toInt() -> EVERY_TWELVE_HOURS.time
                    duration >= TimeUnit.MINUTES.toSeconds(30).toInt() -> EVERY_ONE_HOUR.time
                    duration >= TimeUnit.MINUTES.toSeconds(10).toInt() -> EVERY_THIRTY_MINUTES.time
                    duration >= TimeUnit.MINUTES.toSeconds(1).toInt() -> EVERY_TEN_MINUTES.time
                    duration >= TimeUnit.SECONDS.toSeconds(30).toInt() -> EVERY_ONE_MINUTE.time
                    duration >= TimeUnit.SECONDS.toSeconds(15).toInt() -> EVERY_THIRTY_SECONDS.time
                    else -> EVERY_FIFTEEN_SECONDS.time
                }
            } else {
                timeActual
            }
        }

        fun convertItemOfTimeInSecondsByError(item: Int): Int {
            return when (item) {
                EVERY_TWENTY_FOUR_HOURS_ERROR.time -> TimeUnit.HOURS.toSeconds(
                    24
                ).toInt()
                else -> TimeUnit.DAYS.toSeconds(7).toInt()
            }
        }

        @SuppressLint("SimpleDateFormat")
        fun dateToTimestamp(day: Int, month: Int, year: Int): Long =
            SimpleDateFormat("dd.MM.yyyy").let { formatter ->
                formatter.parse("$day.$month.$year")?.time ?: 0
            }

        fun getTimeWithDays(millisUntilFinished: Long, showHours: Boolean = true): String {
            var duration = ""
            val seconds = (millisUntilFinished / 1000) % 60
            val minutes = (millisUntilFinished / (1000 * 60) % 60)
            val hour = (millisUntilFinished / (1000 * 60 * 60) % 24)
            val days = (millisUntilFinished / (1000 * 60 * 60 * 24))

            if (days > 0) {
                duration += "${days}d "
            }

            if (showHours) {
                duration += if (hour < 10) "0${hour}:" else "$hour:"
            } else if (hour > 0) {
                duration += if (hour < 10) "0${hour}:" else "$hour:"
            }

            duration += if (minutes < 10) "0${minutes}:" else "$minutes:"
            duration += if (seconds < 10) "0${seconds}" else "$seconds"

            return duration
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

        suspend fun copyFile(
            context: Context,
            fileInputStream: FileInputStream,
            subFolder: String,
            fileName: String
        ): File {
            val path = File(context.cacheDir!!, subFolder)
            if (!path.exists())
                path.mkdirs()
            val file = File(path, fileName)

            file.outputStream().use { fileOut ->
                fileInputStream.copyTo(fileOut)
                fileOut.flush()
                fileOut.close()
            }

            fileInputStream.close()

            return file
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

        fun convertFileInputStreamToByteArray(fileInputStream: FileInputStream): ByteArray {
            val outputStream = ByteArrayOutputStream()
            fileInputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            return outputStream.toByteArray()
        }

        fun windowVisibleDisplayFrame(context: Activity): Rect {
            val result = Rect()
            context.window.decorView.getWindowVisibleDisplayFrame(result)
            return result
        }

        @SuppressLint("DiscouragedPrivateApi")
        fun getKeyboardHeight(context: Context): Int {
            val imm = context.applicationContext
                .getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            val inputMethodManagerClass: Class<*> = imm.javaClass
            val visibleHeightMethod =
                inputMethodManagerClass.getDeclaredMethod("getInputMethodWindowVisibleHeight")
            visibleHeightMethod.isAccessible = true
            return visibleHeightMethod.invoke(imm) as Int
        }

        fun getAudioManager(context: Context) =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        internal infix fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
            val safeClickListener = SafeClickListener {
                onSafeClick(it)
            }
            setOnClickListener(safeClickListener)
        }

        fun setupNotificationSound(context: Context, sound: Int) {
            try {
                mediaPlayer.apply {
                    reset()
                    setDataSource(
                        context,
                        Uri.parse("android.resource://" + context.packageName + "/" + sound)
                    )
                    if (isPlaying) {
                        stop()
                        reset()
                        release()
                    }
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        suspend fun isOnlineNet(): Boolean {
            var isOnline = false
            withContext(Dispatchers.IO) {
                isOnline = try {
                    val command = Runtime.getRuntime().exec(Constants.ValidConnection.REQUEST_PIN)
                    val valid = command.waitFor()
                    valid == 0
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            return isOnline
        }

        fun showToast(context: Context, string: String) {
            val vwToast: Toast = Toast.makeText(
                context,
                string,
                Toast.LENGTH_SHORT
            )

            val view = vwToast.view
            view!!.setBackgroundResource(R.drawable.bd_rounded_toast)

            val tv = vwToast.view!!.findViewById<View>(android.R.id.message) as TextView
            tv.gravity = Gravity.CENTER
            tv.textSize = 14F
            vwToast.show()
        }

        fun validateNickname(
            contact: ContactEntity,
            query: String
        ): Boolean {
            val data =
                if (contact.nicknameFake.isEmpty()) contact.nickname else contact.nicknameFake
            return validateSearch(data, query)
        }

        fun validateDisplayName(contact: ContactEntity, query: String): Boolean {
            val data =
                if (contact.displayNameFake.isEmpty()) contact.displayName else contact.displayNameFake
            return validateSearch(data, query)
        }

        private fun validateSearch(data: String, query: String): Boolean {
            return data.toLowerCase(Locale.getDefault()).contains(query)
        }

        fun updateNickNameChannel(
            context: Context,
            contactId: Int,
            oldNick: String,
            newNick: String,
            notificationService: OLD_NotificationService
        ) {
//            val notificationUtils = NotificationService()
//            val notificationUtils = NotificationService(context.applicationContext)
            val uri = notificationService.getChannelSound(
                context,
                Constants.ChannelType.CUSTOM.type,
                contactId,
                oldNick
            )

            deleteUserChannel(notificationService, context, contactId, oldNick)

            updateContactChannel(
                context,
                uri,
                Constants.ChannelType.CUSTOM.type,
                contactId,
                newNick,
                notificationService
            )
        }

        fun updateContactChannel(
            context: Context,
            uri: Uri?,
            channelType: Int,
            contactId: Int? = null,
            contactNick: String? = null,
            notificationService: OLD_NotificationService
        ) {
//            val notificationUtils = NotificationService()
//            val notificationUtils = NotificationService(context.applicationContext)

            notificationService.updateChannel(context, uri, channelType, contactId, contactNick)
        }

        fun deleteUserChannel(
            notificationService: OLD_NotificationService,
            context: Context,
            contactId: Int,
            oldNick: String,
            notificationId: String? = null
        ) {
            Timber.d("*TestDelete: id $contactId, nick $oldNick")
//            val notificationUtils = NotificationService()
//            val notificationUtils = NotificationService(context.applicationContext)
            val channelId = if (notificationId != null) {
                Timber.d("*TestDelete: exist Channel $notificationId")
                context.getString(R.string.notification_custom_channel_id, oldNick, notificationId)
            } else {
                Timber.d("*TestDelete: no exist Channel")
                notificationService.getChannelId(
                    context,
                    Constants.ChannelType.CUSTOM.type,
                    contactId,
                    oldNick
                )
            }

            Timber.d("*TestDelete: ChannelId $channelId")

            notificationService.deleteChannel(context, channelId, contactId)
        }
    }
}