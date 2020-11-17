package com.naposystems.napoleonchat.entity.message.attachments

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import java.util.*

data class MediaStoreAudio(
    val id: Long,
    var displayName: String,
    val dateAdded: Date,
    val size: Long,
    val duration: Long,
    val albumArt: String?,
    val contentUri: Uri,
    var isSelected: Boolean,
    var extension: String?
) {
    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<MediaStoreAudio>() {
            override fun areItemsTheSame(oldItem: MediaStoreAudio, newItem: MediaStoreAudio) =
                oldItem.id == newItem.id && oldItem.isSelected == newItem.isSelected

            override fun areContentsTheSame(oldItem: MediaStoreAudio, newItem: MediaStoreAudio) =
                oldItem == newItem
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaStoreAudio

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}