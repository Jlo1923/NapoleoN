package com.naposystems.pepito.ui.attachmentAudio

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.database.getStringOrNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.entity.message.attachments.MediaStoreAudio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class AttachmentAudioViewModel @Inject constructor(private val context: Context) :
    ViewModel(), IContractAttachmentAudio.ViewModel {

    private var listMediaStoreAudio = mutableListOf<MediaStoreAudio>()

    private val _audios = MutableLiveData<List<MediaStoreAudio>>()
    val audios: LiveData<List<MediaStoreAudio>>
        get() = _audios

    private suspend fun queryAudios(): List<MediaStoreAudio> {
        val audios = mutableListOf<MediaStoreAudio>()

        withContext(Dispatchers.IO) {

            //Columns
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.MIME_TYPE
            )

            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                /*selection*/null,
                /*selectionArgs*/null,
                sortOrder
            )?.use { cursor ->

                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)
                val dateAddedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val dateAdded = Date(cursor.getLong(dateAddedColumn))
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(displayNameColumn)
                    val size = cursor.getLong(sizeColumn)
                    val duration = cursor.getLong(durationColumn)
                    val albumId = cursor.getInt(albumIdColumn)
                    var albumArt: String? = ""
                    val mimeType = cursor.getString(mimeTypeColumn)

                    context.contentResolver.query(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        arrayOf(
                            MediaStore.Audio.Albums._ID,
                            MediaStore.Audio.Albums.ALBUM_ART
                        ),
                        MediaStore.Audio.Albums._ID + "=?",
                        arrayOf(albumId.toString()),
                        null
                    )?.use { cursorAlbum ->

                        val albumArtColumn =
                            cursorAlbum.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART)

                        if (cursorAlbum.moveToFirst()) {
                            albumArt = cursorAlbum.getStringOrNull(albumArtColumn)
                        }
                    }

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val audio = MediaStoreAudio(
                        id = id,
                        displayName = displayName,
                        dateAdded = dateAdded,
                        contentUri = contentUri,
                        size = size,
                        duration = duration,
                        albumArt = albumArt,
                        isSelected = false,
                        extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                    )

                    audios.add(audio)

                    Timber.v(audio.toString())
                }

            }
        }

        return audios
    }

    //region Implementation IContractAttachmentAudio.ViewModel
    override fun loadAudios() {
        viewModelScope.launch {
            listMediaStoreAudio = queryAudios().toMutableList()
            _audios.value = listMediaStoreAudio
        }
    }

    override fun setSelected(mediaStoreAudio: MediaStoreAudio) {
        viewModelScope.launch {

            listMediaStoreAudio.forEachIndexed { index, video ->
                video.takeIf { it.id == mediaStoreAudio.id }?.let {
                    listMediaStoreAudio[index] = it.copy(isSelected = !mediaStoreAudio.isSelected)
                }
            }

            _audios.postValue(listMediaStoreAudio)
        }
    }

    override fun getAudiosSelected() = this.listMediaStoreAudio.filter {
        it.isSelected
    }

    //endregion

}
