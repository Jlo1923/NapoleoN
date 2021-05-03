package com.naposystems.napoleonchat.ui.multipreview.listeners.events

sealed class ViewAttachmentOptionEvent {

    class OnChangeSelfDestruction(
        val iconSelfDestruction: Int
    ) : ViewAttachmentOptionEvent()

    object OnDelete : ViewAttachmentOptionEvent()

}