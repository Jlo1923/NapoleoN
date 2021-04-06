package com.naposystems.napoleonchat.utils.imageCompressor.constraint

import java.io.File

 interface Constraint {
    fun isSatisfied(imageFile: File): Boolean

    fun satisfy(imageFile: File): File
}