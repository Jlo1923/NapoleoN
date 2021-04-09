package com.naposystems.napoleonchat.ui.multipreview.events

import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem

sealed class MultipleAttachmentPreviewState {

    object Loading : MultipleAttachmentPreviewState()

    class SuccessFilesAsPager(
        val listFiles: ArrayList<MultipleAttachmentFileItem>
    ) : MultipleAttachmentPreviewState()

    object Error : MultipleAttachmentPreviewState()

}