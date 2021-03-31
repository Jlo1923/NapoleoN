package com.naposystems.napoleonchat.ui.previewmulti.events

sealed class MultipleAttachmentPreviewAction {

    object Exit : MultipleAttachmentPreviewAction()
    object ShowAttachmentOptions : MultipleAttachmentPreviewAction()
    object ShowAttachmentOptionsWithoutAnim : MultipleAttachmentPreviewAction()
    object HideAttachmentOptions : MultipleAttachmentPreviewAction()
    object HideFileTabs : MultipleAttachmentPreviewAction()

    class ShowSelectFolderName(
        val folderName: String
    ) : MultipleAttachmentPreviewAction()

    class SelectItemInTabLayout(
        val indexItem: Int
    ) : MultipleAttachmentPreviewAction()

    class ShowSelfDestruction(
        val selfDestruction: Int
    ) : MultipleAttachmentPreviewAction()

}