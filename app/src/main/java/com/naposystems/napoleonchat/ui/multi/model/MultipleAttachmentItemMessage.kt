package com.naposystems.napoleonchat.ui.multi.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MultipleAttachmentItemMessage(
    val attachment: MultipleAttachmentItemAttachment,
    val isMine: Int,
    val webId: String,
    val contactId: Int,
    var isRead: Boolean = false
) : Parcelable

