package com.naposystems.pepito.utility

import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.naposystems.pepito.entity.message.attachments.Attachment
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.*
import com.naposystems.pepito.utility.Constants.AttachmentType.*
import com.naposystems.pepito.utility.Constants.NapoleonCacheDirectories.*

class FileManager {
    companion object {

        suspend fun compressImageFromFileInputStream(
            context: Context,
            fileInputStream: FileInputStream
        ): File {
            val fileName = "${System.currentTimeMillis()}.jpg"

            val file = copyFile(
                context,
                fileInputStream,
                Constants.NapoleonCacheDirectories.IMAGES.folder,
                fileName
            )

            val compressedImageFile = Compressor.compress(context, file) {
                resolution(1280, 720)
                quality(80)
            }

            compressedImageFile.renameTo(file)

            return file
        }

        suspend fun copyFile(
            context: Context,
            fileInputStream: FileInputStream,
            subFolder: String,
            fileName: String
        ): File {
            val path = File(context.cacheDir!!, subFolder)
            if (!path.exists())
                path.mkdirs()
            val file = File(path, fileName)

            file.outputStream().use { fileOut ->
                fileInputStream.copyTo(fileOut)
                fileOut.flush()
                fileOut.close()
            }

            withContext(Dispatchers.IO) {
                fileInputStream.close()
            }

            return file
        }

        fun getThumbnailFromVideo(filePath: String): InputStream {
            val thumb = ThumbnailUtils.createVideoThumbnail(
                filePath,
                MediaStore.Images.Thumbnails.MINI_KIND
            )

            val outputStream = ByteArrayOutputStream()
            thumb!!.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val bitmapData: ByteArray = outputStream.toByteArray()
            return ByteArrayInputStream(bitmapData)
        }

        fun getThumbnailFromImage(filePath: String): InputStream {
            val thumb = ThumbnailUtils.createImageThumbnail(
                filePath,
                MediaStore.Images.Thumbnails.MINI_KIND
            )

            val outputStream = ByteArrayOutputStream()
            thumb!!.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val bitmapData: ByteArray = outputStream.toByteArray()
            return ByteArrayInputStream(bitmapData)
        }

        fun getFileType(path: String): String? {
            var type: String? = null
            val extension = MimeTypeMap.getFileExtensionFromUrl(path)
            if (extension != null) {
                val mime = MimeTypeMap.getSingleton()
                type = mime.getMimeTypeFromExtension(extension)
            }
            return type
        }


        fun saveToDisk(context: Context, body: ResponseBody, attachment: Attachment): String {
            try {
                var folder = ""

                when (attachment.type) {
                    Constants.AttachmentType.IMAGE.type -> {
                        folder = Constants.NapoleonCacheDirectories.IMAGES.folder
                    }
                    Constants.AttachmentType.AUDIO.type -> {
                        folder = Constants.NapoleonCacheDirectories.AUDIOS.folder
                    }
                    Constants.AttachmentType.VIDEO.type -> {
                        folder = Constants.NapoleonCacheDirectories.VIDEOS.folder
                    }
                    Constants.AttachmentType.DOCUMENT.type -> {
                        folder = Constants.NapoleonCacheDirectories.DOCUMENTOS.folder
                    }
                }

                val path = File(context.cacheDir!!, folder)
                if (!path.exists())
                    path.mkdirs()
                val file = File(path, "${attachment.webId}.${attachment.extension}")

                /*val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
                val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

                val encryptedFile = EncryptedFile.Builder(
                    audioFile,
                    context,
                    masterKeyAlias,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
                ).build()*/

                var inputStream: InputStream? = null
                var outputStream: OutputStream? = null

                try {
                    Timber.d("File Size=" + body.contentLength())
                    inputStream = body.byteStream()
                    outputStream = file.outputStream() /*encryptedFile.openFileOutput()*/
                    val data = ByteArray(4096)
                    var count: Int
                    var progress = 0
                    while (inputStream.read(data).also { count = it } != -1) {
                        outputStream.write(data, 0, count)
                        progress += count
                        Timber.d(
                            "Progress: " + progress + "/" + body.contentLength() + " >>>> " + progress.toFloat() / body.contentLength()
                        )
                    }
                    outputStream.flush()
                    Timber.d("File saved successfully!")
                    return ""
                } catch (e: IOException) {
                    e.printStackTrace()
                    Timber.d("Failed to save the file!")
                    return ""
                } finally {
                    inputStream?.close()
                    outputStream?.close()
                    return file.name
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Timber.d("Failed to save the file!")
                return ""
            }
        }

        fun getSubfolderByAttachmentType(attachmentType: String): String {
            return when (attachmentType) {
                IMAGE.type -> IMAGES.folder
                AUDIO.type -> AUDIOS.folder
                VIDEO.type -> VIDEOS.folder
                DOCUMENT.type -> DOCUMENTOS.folder
                else -> throw IllegalArgumentException("El archivo a enviar no tiene un tipo")
            }
        }
    }
}