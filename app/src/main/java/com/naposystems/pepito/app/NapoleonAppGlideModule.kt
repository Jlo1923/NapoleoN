package com.naposystems.pepito.app

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.model.attachment.gallery.GalleryItem
import com.naposystems.pepito.utility.glideModelLoaders.attachment.AttachmentLoaderFactory
import com.naposystems.pepito.utility.glideModelLoaders.galleryItem.GalleryItemLoaderFactory
import java.io.InputStream

@GlideModule
class NapoleonAppGlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(
            Attachment::class.java,
            InputStream::class.java,
            AttachmentLoaderFactory(
                context
            )
        )

        registry.prepend(
            GalleryItem::class.java,
            InputStream::class.java,
            GalleryItemLoaderFactory(
                context
            )
        )
    }
}