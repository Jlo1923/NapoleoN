package com.naposystems.napoleonchat.utility.extensions

import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.utility.Constants

fun MultipleAttachmentFileItem.isVideo(): Boolean {
    return this.attachmentType == Constants.AttachmentType.VIDEO.type
}