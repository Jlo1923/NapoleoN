package com.naposystems.napoleonchat.app

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import com.naposystems.napoleonchat.model.attachment.gallery.GalleryItem
import com.naposystems.napoleonchat.utility.glideModelLoaders.attachment.AttachmentLoaderFactory
import com.naposystems.napoleonchat.utility.glideModelLoaders.galleryItem.GalleryItemLoaderFactory
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

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDefaultRequestOptions(
            RequestOptions()
                .disallowHardwareConfig()
        )
    }
}