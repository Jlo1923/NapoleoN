package com.naposystems.napoleonchat.model

data class Theme(
    val id: Int,
    val themeName: String,
    var isSelected: Boolean = false
)