package com.naposystems.pepito.utility.glideModelLoaders.galleryItem

import android.content.Context
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.signature.ObjectKey
import com.naposystems.pepito.model.attachment.gallery.GalleryItem
import java.io.FileInputStream
import java.io.InputStream

class GalleryItemModelLoader constructor(private val context: Context) :
    ModelLoader<GalleryItem, InputStream> {

    override fun buildLoadData(
        model: GalleryItem,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream>? {
        val diskCacheKey: Key = ObjectKey(model)

        return ModelLoader.LoadData(
            diskCacheKey,
            GalleryItemDataFetcher(
                context = context,
                galleryItem = model
            )
        )
    }

    override fun handles(model: GalleryItem) = true
}