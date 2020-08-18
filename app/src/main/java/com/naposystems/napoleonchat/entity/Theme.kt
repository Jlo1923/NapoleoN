package com.naposystems.napoleonchat.entity

data class Theme(
    val id: Int,
    val themeName: String,
    var isSelected: Boolean = false
)