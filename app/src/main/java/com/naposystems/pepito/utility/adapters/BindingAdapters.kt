package com.naposystems.pepito.utility.adapters

import android.Manifest
import android.app.Service
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
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
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.naposystems.pepito.R
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.model.conversationCall.ConversationCall
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.GlideManager
import com.naposystems.pepito.utility.Utils
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
                        requireContext(),
                        childFragmentManager,
                        drawableIconId,
                        message,
                        { Utils.openSetting(requireContext()) },
                        {}
                    )
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                Utils.showDialogToInformPermission(
                    requireContext(),
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
        channel = "private-${getString(Constants.CallKeys.CHANNEL)}"
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
fun bindAvatar(imageView: ImageView, @Nullable contact: Contact?) {
    val context = imageView.context
    if (contact != null && contact.id != 0) {

        val defaultAvatar = context.resources.getDrawable(
            R.drawable.ic_default_avatar,
            context.theme
        )

        val loadImage = when {
            contact.imageUrlFake.isNotEmpty() -> {
                Utils.getFileUri(
                    context = context,
                    fileName = contact.imageUrlFake,
                    subFolder = Constants.NapoleonCacheDirectories.IMAGE_FAKE_CONTACT.folder
                )
            }
            contact.imageUrl.isNotEmpty() -> {
                contact.imageUrl
            }
            else -> {
                ""
            }
        }

        Glide.with(context)
            .load(if (loadImage != "") loadImage else defaultAvatar)
            .transform(CircleCrop(), CenterInside())
            .into(imageView)

        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
    } else {
        val addContact = context.resources.getDrawable(
            R.drawable.ic_person_add,
            context.theme
        )

        imageView.setImageDrawable(addContact)
        imageView.scaleType = ImageView.ScaleType.CENTER
    }
}

@BindingAdapter("avatarWithoutCircle")
fun bindAvatarWithoutCircle(imageView: ImageView, @Nullable contact: Contact?) {
    if (contact != null) {
        val context = imageView.context

        val defaultAvatar = context.resources.getDrawable(
            R.drawable.ic_default_avatar,
            context.theme
        )

        @Suppress("IMPLICIT_CAST_TO_ANY")
        val loadImage = when {
            contact.imageUrlFake.isNotEmpty() -> {
                Utils.getFileUri(
                    context = context,
                    fileName = contact.imageUrlFake,
                    subFolder = Constants.NapoleonCacheDirectories.IMAGE_FAKE_CONTACT.folder
                )
            }
            contact.imageUrl.isNotEmpty() -> {
                contact.imageUrl
            }
            else -> {
                defaultAvatar
            }
        }

        Glide.with(imageView)
            .load(loadImage)
            .into(imageView)
    }
}

@BindingAdapter("nickname")
fun bindNickname(textView: TextView, @Nullable contact: Contact?) {
    textView.context.let { context ->
        textView.text = context.getString(R.string.label_nickname, contact?.getNickName())
    }
}

@BindingAdapter("name")
fun bindName(textView: TextView, @Nullable contact: Contact?) {
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
    if(contactId == 0) {
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

