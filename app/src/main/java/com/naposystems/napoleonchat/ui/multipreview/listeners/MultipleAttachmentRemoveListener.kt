package com.naposystems.napoleonchat.ui.multipreview.listeners

import com.naposystems.napoleonchat.ui.multipreview.listeners.events.MultipleAttachmentRemoveEvent

interface MultipleAttachmentRemoveListener {

    fun onRemoveAttachment(event: MultipleAttachmentRemoveEvent)

}