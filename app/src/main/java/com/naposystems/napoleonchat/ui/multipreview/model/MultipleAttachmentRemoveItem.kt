package com.naposystems.napoleonchat.ui.multipreview.model

const val MODE_CREATE = 1
const val MODE_RECEIVER = 2
const val MODE_SENDER = 3

data class MultipleAttachmentRemoveItem(
    val title: String,
    val message: String,
    val option1: String,
    val option2: String? = null,
    val cancelText: String,
    val modeDelete: Int
)

