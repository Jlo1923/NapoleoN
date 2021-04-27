package com.naposystems.napoleonchat.ui.multipreview.listeners

import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem

interface MultipleAttachmentPreviewListener {

    fun changeVisibilityOptions()

    fun forceShowOptions()

    fun markAttachmentAsRead(attachmentEntity: MultipleAttachmentFileItem)

}