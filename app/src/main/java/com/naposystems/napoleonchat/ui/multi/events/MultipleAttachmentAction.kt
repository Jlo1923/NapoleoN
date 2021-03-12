package com.naposystems.napoleonchat.ui.multi.events

import com.xwray.groupie.Item

sealed class MultipleAttachmentAction {

    object Exit : MultipleAttachmentAction()

    object BackToFolderList : MultipleAttachmentAction()

    class ShowSelectFolderName(
        val folderName: String
    ) : MultipleAttachmentAction()

    class ShowPreviewSelectedFiles(
        val listElements: List<Item<*>>
    ) : MultipleAttachmentAction()

    object HideListSelectedFiles : MultipleAttachmentAction()

    object ShowHasMaxFilesAttached : MultipleAttachmentAction()

}