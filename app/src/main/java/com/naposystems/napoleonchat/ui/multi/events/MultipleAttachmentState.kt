package com.naposystems.napoleonchat.ui.multi.events

import com.xwray.groupie.Item

sealed class MultipleAttachmentState {

    object Loading : MultipleAttachmentState()

    class SuccessFolders(
        val listElements: List<Item<*>>
    ) : MultipleAttachmentState()

    class SuccessFiles(
        val listElements: List<Item<*>>
    ) : MultipleAttachmentState()

    object Error : MultipleAttachmentState()

}