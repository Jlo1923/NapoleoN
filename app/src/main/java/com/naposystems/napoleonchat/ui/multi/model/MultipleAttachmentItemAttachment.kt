package com.naposystems.napoleonchat.ui.multi.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MultipleAttachmentItemAttachment(
    val fileName: String,
    val status: Int,
    val webId: String,
    val extension: String = "",
    var body: String
) : Parcelable

