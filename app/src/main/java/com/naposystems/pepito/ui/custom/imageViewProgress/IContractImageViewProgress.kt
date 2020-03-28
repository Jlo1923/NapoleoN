package com.naposystems.pepito.ui.custom.imageViewProgress

import java.io.File

interface IContractImageViewProgress {

    fun loadImageFile(file: File)

    fun loadImageFileAsGif(file: File)
}