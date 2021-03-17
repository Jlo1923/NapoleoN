package com.naposystems.napoleonchat.ui.previewmulti.events

sealed class MultipleAttachmentPreviewAction {

    object Exit : MultipleAttachmentPreviewAction()
    object ShowAttachmentOptions : MultipleAttachmentPreviewAction()
    object HideAttachmentOptions : MultipleAttachmentPreviewAction()

    class ShowSelectFolderName(
        val folderName: String
    ) : MultipleAttachmentPreviewAction()

}