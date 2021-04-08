package com.naposystems.napoleonchat.utility.extensions

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.webkit.MimeTypeMap

fun Intent.isActionSend(): Boolean =
    action?.let { action == Intent.ACTION_SEND } ?: kotlin.run { false }

fun Intent.isActionSendMultiple(): Boolean =
    action?.let { action == Intent.ACTION_SEND_MULTIPLE } ?: kotlin.run { false }

fun Intent.isTypeVideoOrImage(): Boolean = isTypeVideo() || isTypeImage()

fun Intent.isTypeAnyOrVideoOrImage(): Boolean = isTypeVideoOrImage() || isTypeAny()

private fun Intent.isTypeImage(): Boolean =
    type?.startsWith("image/") ?: kotlin.run { false }

private fun Intent.isTypeVideo(): Boolean =
    type?.startsWith("video/") ?: kotlin.run { false }

private fun Intent.isTypeAny(): Boolean =
    type?.startsWith("\\*/") ?: kotlin.run { false }

fun Intent.getUriListFromExtra(): List<Uri> {
    val uris = getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)
    return uris?.map {
        it as Uri
    } ?: kotlin.run { emptyList() }
}

fun getMimeType(uri: Uri, context: Context): String? {
    var mimeType: String? = null
    mimeType = if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
        val cr: ContentResolver = context.contentResolver
        cr.getType(uri)
    } else {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(
            uri.toString()
        )
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            fileExtension.toLowerCase()
        )
    }
    return mimeType
}



