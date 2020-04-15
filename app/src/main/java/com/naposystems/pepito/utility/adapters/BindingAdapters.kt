package com.naposystems.pepito.utility.adapters

import android.Manifest
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.naposystems.pepito.R
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.model.conversationCall.ConversationCall
import com.naposystems.pepito.utility.Constants
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

    Dexter.withActivity(activity!!)
        .withPermissions(*permissions)
        .withListener(object : MultiplePermissionsListener {

            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    successCallback()
                }

                if (report.isAnyPermissionPermanentlyDenied) {
                    Utils.showDialogToInformPermission(
                        context!!,
                        childFragmentManager,
                        drawableIconId,
                        message,
                        { Utils.openSetting(context!!) },
                        {}
                    )
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                Utils.showDialogToInformPermission(
                    context!!,
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

        Glide.with(context)
            .load(loadImage)
            .circleCrop()
            .into(imageView)
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
    when(format) {
        Constants.UserDisplayFormat.ONLY_NICKNAME.format -> {
            textView.visibility = View.GONE
        }
        else -> {
            textView.visibility = View.VISIBLE
        }
    }
}

@BindingAdapter("nickNameFormat")
fun bindNickNameFormat(textView: TextView, format: Int) {
    when(format) {
        Constants.UserDisplayFormat.ONLY_NAME.format -> {
            textView.visibility = View.GONE
        }
        else -> {
            textView.visibility = View.VISIBLE
        }
    }
}

