package com.naposystems.napoleonchat.ui.previewmulti.listeners

import com.naposystems.napoleonchat.ui.previewmulti.listeners.events.MultipleAttachmentRemoveEvent

interface MultipleAttachmentRemoveListener {

    fun onRemoveAttachment(event: MultipleAttachmentRemoveEvent)

}