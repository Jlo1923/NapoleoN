package com.naposystems.pepito.model.attachment.gallery

sealed class GalleryResult {
    object Loading : GalleryResult()
    data class Success(val listGalleryFolder: List<GalleryFolder>): GalleryResult()
    data class Error(val message: String): GalleryResult()
}