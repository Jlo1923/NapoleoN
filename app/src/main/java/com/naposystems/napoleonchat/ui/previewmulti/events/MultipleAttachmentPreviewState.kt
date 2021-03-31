package com.naposystems.napoleonchat.ui.previewmulti.events

import com.xwray.groupie.Item

sealed class MultipleAttachmentPreviewState {

    object Loading : MultipleAttachmentPreviewState()

    class SuccessFilesAsPager(
        val listElements: List<Item<*>>
    ) : MultipleAttachmentPreviewState()

    class SuccessFiles(
        val listElements: List<Item<*>>
    ) : MultipleAttachmentPreviewState()

    object Error : MultipleAttachmentPreviewState()

}