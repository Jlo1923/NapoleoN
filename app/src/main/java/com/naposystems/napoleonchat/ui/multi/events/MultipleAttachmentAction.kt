package com.naposystems.napoleonchat.ui.multi.events

sealed class MultipleAttachmentAction {

    object Exit : MultipleAttachmentAction()

    object BackToFolderList : MultipleAttachmentAction()

    class ShowListSelectedFiles(
        val folderName: String
    ) : MultipleAttachmentAction()

    object HideListSelectedFiles : MultipleAttachmentAction()

}