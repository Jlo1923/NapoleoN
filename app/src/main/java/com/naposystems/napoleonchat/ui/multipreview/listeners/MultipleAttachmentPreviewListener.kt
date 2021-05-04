package com.naposystems.napoleonchat.ui.multipreview.listeners

import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentItemAttachment

interface MultipleAttachmentPreviewListener {

    fun changeVisibilityOptions()

    fun forceShowOptions()

    fun markAttachmentAsRead(attachmentEntity: MultipleAttachmentFileItem)

    fun deleteAttachmentByDestructionTime(
        attachmentWebId: String,
        position: Int
    )

}