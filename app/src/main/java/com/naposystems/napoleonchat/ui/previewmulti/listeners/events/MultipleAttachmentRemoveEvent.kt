package com.naposystems.napoleonchat.ui.previewmulti.listeners.events

sealed class MultipleAttachmentRemoveEvent {

    object OnSimpleRemove : MultipleAttachmentRemoveEvent()
    object OnRemoveForAll : MultipleAttachmentRemoveEvent()
    object OnRemoveForSender : MultipleAttachmentRemoveEvent()
    object OnRemoveForRecipient : MultipleAttachmentRemoveEvent()

}