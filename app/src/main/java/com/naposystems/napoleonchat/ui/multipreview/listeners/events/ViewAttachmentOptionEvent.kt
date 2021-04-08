package com.naposystems.napoleonchat.ui.multipreview.listeners.events

sealed class ViewAttachmentOptionEvent {

    object OnChangeSelfDestruction : ViewAttachmentOptionEvent()

    object OnDelete : ViewAttachmentOptionEvent()

}