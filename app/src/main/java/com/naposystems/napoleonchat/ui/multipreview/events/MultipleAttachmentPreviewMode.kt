package com.naposystems.napoleonchat.ui.multipreview.events

sealed class MultipleAttachmentPreviewMode {

    object ModeCreate : MultipleAttachmentPreviewMode()

    class ModeView(
        val messageText: String
    ) : MultipleAttachmentPreviewMode()

}