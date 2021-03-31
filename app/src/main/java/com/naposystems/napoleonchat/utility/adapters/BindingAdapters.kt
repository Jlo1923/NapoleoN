package com.naposystems.napoleonchat.utility.adapters

import android.Manifest
import android.app.Service
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.ContextThemeWrapper
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.textfield.TextInputEditText
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.model.conversationCall.ConversationCall
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.GlideManager
import com.naposystems.napoleonchat.utility.Utils
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

fun Fragment.verifyPermission(
    vararg permissions: String,
    drawableIconId: Int,
    message: Int,
    successCallback: () -> Unit
) {

    Dexter.withContext(requireContext())
        .withPermissions(*permissions)
        .withListener(object : MultiplePermissionsListener {

            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    successCallback()
                }

                if (report.isAnyPermissionPermanentlyDenied) {
                    Utils.showDialogToInformPermission(
                        requireActivity(),
                        childFragmentManager,
                        drawableIconId,
                        message,
                        { Utils.openSetting(requireActivity()) },
                        {}
                    )
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                Utils.showDialogToInformPermission(
                    requireActivity(),
                    childFragmentManager,
                    drawableIconId,
                    message,
                    { token!!.continuePermissionRequest() },
                    { token!!.cancelPermissionRequest() }
                )
            }
        }).check()
}

fun Fragment.verifyCameraAndMicPermission(successCallback: () -> Unit) {
    this.verifyPermission(
        Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
        drawableIconId = R.drawable.ic_camera_primary,
        message = R.string.text_explanation_camera_to_attachment_picture
    ) {
        successCallback()
    }
}

fun Fragment.verifyBatteryOptimization(successCallback: () -> Unit) {

}

fun Fragment.showToast(message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.hasMicAndCameraPermission(): Boolean {
    val checkSelfMicPermission =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
    val checkSelfCameraPermission =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
    return (checkSelfMicPermission == PackageManager.PERMISSION_GRANTED &&
            checkSelfCameraPermission == PackageManager.PERMISSION_GRANTED)
}

fun Service.hasMicAndCameraPermission(): Boolean {
    val checkSelfMicPermission =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
    val checkSelfCameraPermission =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
    return (checkSelfMicPermission == PackageManager.PERMISSION_GRANTED &&
            checkSelfCameraPermission == PackageManager.PERMISSION_GRANTED)
}

fun JSONObject.toIceCandidate(): IceCandidate {
    val data = this
    return IceCandidate(data.getString("id"), data.getInt("label"), data.getString("candidate"))
}

fun JSONObject.toSessionDescription(type: SessionDescription.Type): SessionDescription {
    val data = this
    return SessionDescription(type, data.getString("sdp"))
}

fun JSONObject.toConversationCallModel(): ConversationCall {
    var channel = ""
    var contactId = 0
    var isVideoCall = false

    if (has(Constants.CallKeys.CHANNEL)) {
        channel = "presence-${getString(Constants.CallKeys.CHANNEL)}"
    }

    if (has(Constants.CallKeys.CONTACT_ID)) {
        contactId = getInt(Constants.CallKeys.CONTACT_ID)
    }

    if (has(Constants.CallKeys.IS_VIDEO_CALL)) {
        isVideoCall = getBoolean(Constants.CallKeys.IS_VIDEO_CALL)
    }

    return ConversationCall(channel, contactId, isVideoCall)
}

fun IceCandidate.toJSONObject(): JSONObject {
    val jsonObject = JSONObject()
    jsonObject.put("type", "candidate")
    jsonObject.put("label", sdpMLineIndex)
    jsonObject.put("id", sdpMid)
    jsonObject.put("candidate", sdp)

    return jsonObject
}

fun SessionDescription.toJSONObject(): JSONObject {
    val jsonObject = JSONObject()

    jsonObject.put("type", type.canonicalForm())
    jsonObject.put("sdp", description)

    return jsonObject
}

fun View.slideUp(animDuration: Long) {
    visibility = View.VISIBLE
    val animate = TranslateAnimation(
        0f,  // fromXDelta
        0f,  // toXDelta
        height.toFloat(),  // fromYDelta
        0f
    ) // toYDelta
    animate.interpolator = LinearInterpolator()
    animate.duration = animDuration
    startAnimation(animate)
}

fun ImageView.loadContentUri(contentUri: Uri) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.createSource(context.contentResolver, contentUri)
            .also { source ->
                ImageDecoder.decodeBitmap(source).also { bitmap ->
                    GlideManager.loadBitmap(
                        this,
                        bitmap
                    )
                }
            }
    } else {
        val bitmap =
            MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                contentUri
            )
        GlideManager.loadBitmap(
            this,
            bitmap
        )
    }
}

@BindingAdapter("background")
fun bindBackground(imageView: ImageView, imageUrl: String) {
    val context = imageView.context

    val defaultBackground = context.resources.getDrawable(
        R.drawable.bg_default_drawer_header,
        context.theme
    )

    Glide.with(context)
        .load(
            if (imageUrl.isEmpty()) defaultBackground else Uri.parse(imageUrl)
        )
        .into(imageView)
}

@BindingAdapter("avatar")
fun bindAvatar(imageView: ImageView, @Nullable contact: ContactEntity?) {
    val context = imageView.context
    if (contact != null && contact.id != 0) {

        val defaultAvatar = ContextCompat.getDrawable(context, R.drawable.ic_default_avatar)

        Glide.with(context)
            .load(contact.imageUrlFake)
            .apply(
                RequestOptions()
                    .priority(Priority.NORMAL)
                    .fitCenter()
            ).error(defaultAvatar)
            .circleCrop()
            .into(imageView)

    } else {
        val addContact = ContextCompat.getDrawable(context, R.drawable.ic_person_add)
        imageView.setImageDrawable(addContact)
        imageView.scaleType = ImageView.ScaleType.CENTER
    }
}

@BindingAdapter("avatarWithoutCircle")
fun bindAvatarWithoutCircle(imageView: ImageView, @Nullable contact: ContactEntity?) {
    if (contact != null) {
        val context = imageView.context

        val defaultAvatar = ContextCompat.getDrawable(context, R.drawable.ic_default_avatar)

        Glide.with(context)
            .load(contact.imageUrlFake)
            .apply(
                RequestOptions()
                    .priority(Priority.NORMAL)
                    .fitCenter()
            ).error(defaultAvatar)
            .into(imageView)
    }
}

@BindingAdapter("nickname")
fun bindNickname(textView: TextView, @Nullable contact: ContactEntity?) {
    textView.context.let { context ->
        textView.text = context.getString(R.string.label_nickname, contact?.getNickName())
    }
}

@BindingAdapter("name")
fun bindName(textView: TextView, @Nullable contact: ContactEntity?) {
    textView.text = contact?.getName()
}

@BindingAdapter("nameFormat")
fun bindNameFormat(textView: TextView, format: Int) {
    when (format) {
        Constants.UserDisplayFormat.ONLY_NICKNAME.format -> {
            textView.visibility = View.GONE
        }
        else -> {
            textView.visibility = View.VISIBLE
        }
    }
}

@BindingAdapter("nickNameFormat", "contactIdNickNameFormat")
fun bindNickNameFormat(textView: TextView, format: Int, contactId: Int) {
    if (contactId == 0) {
        textView.visibility = View.GONE
    } else {
        when (format) {
            Constants.UserDisplayFormat.ONLY_NAME.format -> {
                textView.visibility = View.GONE
            }
            else -> {
                textView.visibility = View.VISIBLE
            }
        }
    }
}

@BindingAdapter("nameFormatContact", "contactIdNameFormat")
fun bindNameFormatContact(textView: TextView, format: Int, contactId: Int) {
    if (contactId != 0) {
        when (format) {
            Constants.UserDisplayFormat.ONLY_NICKNAME.format -> {
                textView.visibility = View.GONE
            }
            else -> {
                if (textView.text == " ") {
                    textView.visibility = View.GONE
                } else {
                    textView.visibility = View.VISIBLE
                }
            }
        }
    }
}

@BindingAdapter("styleChangeName")
fun bindStyleChangeName(textInputEditText: TextInputEditText, location: Int) {
    textInputEditText.apply {
        when (location) {
            Constants.ChangeParams.NAME_USER.option,
            Constants.ChangeParams.NAME_FAKE.option -> {

                ContextThemeWrapper(context, R.style.OnlyLetters)
            }
            Constants.ChangeParams.NICKNAME_FAKE.option -> {
                ContextThemeWrapper(context, R.style.OnlyLettersUpperCaseAndNumbers)
            }
        }
    }
}
