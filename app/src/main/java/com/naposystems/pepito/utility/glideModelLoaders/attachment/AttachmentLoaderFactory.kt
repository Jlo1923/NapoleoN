package com.naposystems.pepito.utility.glideModelLoaders.attachment

import android.content.Context
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.naposystems.pepito.entity.message.attachments.Attachment
import java.io.InputStream

class AttachmentLoaderFactory constructor(private val context: Context) :
    ModelLoaderFactory<Attachment, InputStream> {

    override fun build(multiFactory: MultiModelLoaderFactory) =
        AttachmentModelLoader(
            context = context
        )

    override fun teardown() {
        //Do nothing
    }
}