package com.naposystems.napoleonchat.utility.glideModelLoaders.attachment

import android.content.Context
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.signature.ObjectKey
import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import java.io.InputStream

class AttachmentModelLoader constructor(private val context: Context) :
    ModelLoader<Attachment, InputStream> {

    override fun buildLoadData(
        model: Attachment,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream>? {
        val diskCacheKey: Key = ObjectKey(model)

        return ModelLoader.LoadData(
            diskCacheKey,
            AttachmentDataFetcher(
                context = context,
                attachment = model
            )
        )
    }

    override fun handles(model: Attachment) = true
}