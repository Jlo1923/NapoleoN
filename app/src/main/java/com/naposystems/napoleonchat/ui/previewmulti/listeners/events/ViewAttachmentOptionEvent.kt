package com.naposystems.napoleonchat.ui.previewmulti.listeners.events

sealed class ViewAttachmentOptionEvent {

    object OnChangeSelfDestruction : ViewAttachmentOptionEvent()
    object OnDelete : ViewAttachmentOptionEvent()

}