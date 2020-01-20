package com.naposystems.pepito.entity

data class Theme(
    val id: Int,
    val themeName: String,
    var isSelected: Boolean = false
)