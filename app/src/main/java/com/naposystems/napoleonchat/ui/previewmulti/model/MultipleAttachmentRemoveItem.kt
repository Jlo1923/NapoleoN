package com.naposystems.napoleonchat.ui.previewmulti.model

data class MultipleAttachmentRemoveItem(
    val title: String,
    val message: String,
    val option1: String,
    val option2: String? = null,
    val cancelText: String
)

