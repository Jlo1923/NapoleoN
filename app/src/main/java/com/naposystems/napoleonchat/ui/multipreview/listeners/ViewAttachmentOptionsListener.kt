package com.naposystems.napoleonchat.ui.multipreview.listeners

import com.naposystems.napoleonchat.ui.multipreview.listeners.events.ViewAttachmentOptionEvent

interface ViewAttachmentOptionsListener {

    fun onViewAttachmentOptionEvent(event: ViewAttachmentOptionEvent)
}