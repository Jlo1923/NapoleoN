package com.naposystems.napoleonchat.model.attachment.gallery

import java.lang.Exception

sealed class GalleryResult {
    object Loading : GalleryResult()
    data class Success(val listGalleryFolder: List<GalleryFolder>): GalleryResult()
    data class Error(val message: String, val exception: Exception): GalleryResult()
}