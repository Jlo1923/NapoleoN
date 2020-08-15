package com.naposystems.napoleonchat.utility.glideModelLoaders.galleryItem

import android.content.Context
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.naposystems.napoleonchat.model.attachment.gallery.GalleryItem
import java.io.InputStream

class GalleryItemLoaderFactory constructor(private val context: Context) :
    ModelLoaderFactory<GalleryItem, InputStream> {

    override fun build(multiFactory: MultiModelLoaderFactory) = GalleryItemModelLoader(context)

    override fun teardown() {
        //Do nothing
    }
}