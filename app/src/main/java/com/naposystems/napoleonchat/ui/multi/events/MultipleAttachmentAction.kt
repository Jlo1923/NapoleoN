package com.naposystems.napoleonchat.ui.multi.events

import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.xwray.groupie.Item

sealed class MultipleAttachmentAction {

    object Exit : MultipleAttachmentAction()

    object BackToFolderList : MultipleAttachmentAction()

    object ShowDialogConfirmExit : MultipleAttachmentAction()

    class ShowSelectFolderName(
        val folderName: String
    ) : MultipleAttachmentAction()

    class ShowPreviewSelectedFiles(
        val listElements: List<Item<*>>
    ) : MultipleAttachmentAction()

    object HideListSelectedFiles : MultipleAttachmentAction()

    object ShowHasMaxFilesAttached : MultipleAttachmentAction()

    class ContinueToPreview(
        val listElements: List<MultipleAttachmentFileItem>
    ) : MultipleAttachmentAction()

}