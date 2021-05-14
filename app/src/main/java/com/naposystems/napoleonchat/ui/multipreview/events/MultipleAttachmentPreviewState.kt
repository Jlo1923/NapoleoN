package com.naposystems.napoleonchat.ui.multipreview.events

import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem

sealed class MultipleAttachmentPreviewState {

    object Loading : MultipleAttachmentPreviewState()

    class SuccessFilesAsPager(
        val listFiles: ArrayList<MultipleAttachmentFileItem>,
        val indexToSelect: Int? = null
    ) : MultipleAttachmentPreviewState()

    object Error : MultipleAttachmentPreviewState()

}