package com.naposystems.napoleonchat.ui.custom.inputPanel

import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation

interface IContractInputPanelQuote {
    fun setupMessageAndAttachment(messageAndAttachmentRelation: MessageAttachmentRelation)
    fun closeQuote()
    fun resetImage()
    fun getMessageAndAttachment(): MessageAttachmentRelation?
}