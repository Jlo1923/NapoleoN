package com.naposystems.napoleonchat.ui.multi.model

import android.net.Uri

data class MultipleAttachmentFileItem(
    val id: Int,
    val attachmentType: String = "",
    var contentUri: Uri? = null,
    val isSelected: Boolean
) {

    override fun equals(other: Any?): Boolean {
        return id == (other as MultipleAttachmentFileItem).id
    }
}

