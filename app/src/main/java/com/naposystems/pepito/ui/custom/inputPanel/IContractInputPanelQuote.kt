package com.naposystems.pepito.ui.custom.inputPanel

import com.naposystems.pepito.entity.message.MessageAndAttachment

interface IContractInputPanelQuote {
    fun setupMessageAndAttachment(messageAndAttachment: MessageAndAttachment)
    fun closeQuote()
    fun resetImage()
    fun getMessageAndAttachment(): MessageAndAttachment?
}