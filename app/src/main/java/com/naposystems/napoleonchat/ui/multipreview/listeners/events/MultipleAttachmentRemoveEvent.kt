package com.naposystems.napoleonchat.ui.multipreview.listeners.events

sealed class MultipleAttachmentRemoveEvent {

    object OnSimpleRemove : MultipleAttachmentRemoveEvent()

    object OnRemoveForAll : MultipleAttachmentRemoveEvent()

    object OnRemoveForTheUser : MultipleAttachmentRemoveEvent()

}