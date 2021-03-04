package com.naposystems.napoleonchat.utility.glideModelLoaders.attachment

import android.content.Context
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.signature.ObjectKey
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import java.io.InputStream

class AttachmentModelLoader constructor(private val context: Context) :
    ModelLoader<AttachmentEntity, InputStream> {

    override fun buildLoadData(
        attachmentEntity: AttachmentEntity,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream>? {
        val diskCacheKey: Key = ObjectKey(attachmentEntity)

        return ModelLoader.LoadData(
            diskCacheKey,
            AttachmentDataFetcher(
                context = context,
                attachmentEntity = attachmentEntity
            )
        )
    }

    override fun handles(model: AttachmentEntity) = true
}