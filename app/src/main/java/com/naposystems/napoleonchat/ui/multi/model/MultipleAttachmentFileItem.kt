package com.naposystems.napoleonchat.ui.multi.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MultipleAttachmentFileItem(
    val id: Int,
    val attachmentType: String = "",
    var contentUri: Uri? = null,
    val isSelected: Boolean,
    var selfDestruction: Int = 0,
    val messageAndAttachment: MultipleAttachmentItemMessage? = null
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        return id == (other as MultipleAttachmentFileItem).id
    }

}

