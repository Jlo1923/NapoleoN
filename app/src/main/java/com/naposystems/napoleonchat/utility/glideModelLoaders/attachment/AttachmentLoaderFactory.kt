package com.naposystems.napoleonchat.utility.glideModelLoaders.attachment

import android.content.Context
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import java.io.InputStream

class AttachmentLoaderFactory constructor(private val context: Context) :
    ModelLoaderFactory<AttachmentEntity, InputStream> {

    override fun build(multiFactory: MultiModelLoaderFactory) =
        AttachmentModelLoader(
            context = context
        )

    override fun teardown() {
        //Do nothing
    }
}