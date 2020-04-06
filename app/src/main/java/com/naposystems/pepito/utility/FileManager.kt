package com.naposystems.pepito.utility

import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.lifecycle.MutableLiveData
import com.naposystems.pepito.utility.Constants.AttachmentType.*
import com.naposystems.pepito.utility.Constants.NapoleonCacheDirectories.*
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.*

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
                IMAGES.folder,
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
            fileInputStream: InputStream,
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


        fun saveToDisk(
            context: Context,
            body: ResponseBody,
            type: String,
            extension: String,
            progressLiveData: MutableLiveData<Float>? = null
        ): String {
            try {

                var folder = ""

                when (type) {
                    IMAGE.type -> {
                        folder = IMAGES.folder
                    }
                    AUDIO.type -> {
                        folder = AUDIOS.folder
                    }
                    VIDEO.type -> {
                        folder = VIDEOS.folder
                    }
                    DOCUMENT.type -> {
                        folder = DOCUMENTOS.folder
                    }
                    GIF.type -> {
                        folder = GIFS.folder
                    }
                    GIF_NN.type -> {
                        folder = GIFS.folder
                    }
                    LOCATION.type -> {
                        folder = IMAGES.folder
                    }
                }

                val path = File(context.cacheDir!!, folder)
                if (!path.exists())
                    path.mkdirs()
                val file = File(path, "${System.currentTimeMillis()}.${extension}")

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
                        progressLiveData?.value = progress.toFloat() / body.contentLength()
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
                GIF.type, GIF_NN.type -> GIFS.folder
                LOCATION.type -> IMAGES.folder
                else -> throw IllegalArgumentException("El archivo a enviar no tiene un tipo")
            }
        }

        fun createFile(context: Context, fileName: String, folder: String): File {

            val path = File(context.cacheDir!!, folder)
            if (!path.exists())
                path.mkdirs()

            return File(path, fileName)
        }

        fun createFileFromBitmap(
            context: Context,
            fileName: String,
            folder: String,
            bitmap: Bitmap
        ): File? {
            val file = createFile(context, fileName, folder)

            return try {
                val stream: OutputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                stream.flush()
                stream.close()

                file
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }
}