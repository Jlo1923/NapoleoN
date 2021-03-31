package com.naposystems.napoleonchat.ui.previewmulti.listeners

import com.naposystems.napoleonchat.ui.previewmulti.listeners.events.ViewAttachmentOptionEvent

interface ViewAttachmentOptionsListener {

    fun onViewAttachmentOptionEvent(event: ViewAttachmentOptionEvent)
}