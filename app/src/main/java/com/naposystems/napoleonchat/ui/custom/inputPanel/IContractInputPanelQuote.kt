package com.naposystems.napoleonchat.ui.custom.inputPanel

import com.naposystems.napoleonchat.entity.message.MessageAndAttachment

interface IContractInputPanelQuote {
    fun setupMessageAndAttachment(messageAndAttachment: MessageAndAttachment)
    fun closeQuote()
    fun resetImage()
    fun getMessageAndAttachment(): MessageAndAttachment?
}