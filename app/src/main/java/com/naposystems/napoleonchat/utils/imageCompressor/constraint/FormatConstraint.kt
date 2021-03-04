package com.naposystems.napoleonchat.utils.imageCompressor.constraint

import android.graphics.Bitmap
import com.naposystems.napoleonchat.utils.imageCompressor.compressFormat
import com.naposystems.napoleonchat.utils.imageCompressor.loadBitmap
import com.naposystems.napoleonchat.utils.imageCompressor.overWrite
import java.io.File

class FormatConstraint(private val format: Bitmap.CompressFormat) : Constraint {

    override fun isSatisfied(imageFile: File): Boolean {
        return format == imageFile.compressFormat()
    }

    override fun satisfy(imageFile: File): File {
        return overWrite(imageFile, loadBitmap(imageFile), format)
    }
}

fun Compression.format(format: Bitmap.CompressFormat) {
    constraint(FormatConstraint(format))
}