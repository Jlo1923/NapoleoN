package com.naposystems.napoleonchat.repository.napoleonKeyboardGif

import android.content.Context
import com.naposystems.napoleonchat.ui.napoleonKeyboardGif.IContractNapoleonKeyboardGif
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.DownloadFileResult
import com.naposystems.napoleonchat.webService.NapoleonApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class NapoleonKeyboardGifRepository @Inject constructor(
    private val context: Context,
    private val napoleonApi: NapoleonApi
) :
    IContractNapoleonKeyboardGif.Repository {

    override suspend fun downloadGif(
        url: String
    ) = flow {
        emit(DownloadFileResult.Progress(1L))
        withContext(Dispatchers.IO) {
            val responseDownloadFile =
                napoleonApi.downloadFileByUrl(url)

            if (responseDownloadFile.isSuccessful) {
                try {
                    val body = responseDownloadFile.body()!!
                    val folder = Constants.CacheDirectories.GIFS.folder
                    val fileName = "${System.currentTimeMillis()}.gif"

                    val path = File(context.cacheDir!!, folder)
                    if (!path.exists())
                        path.mkdirs()
                    val file = File(path, fileName)

                    var inputStream: InputStream? = null
                    var outputStream: OutputStream? = null

                    try {
                        val contentLength = body.contentLength()
                        Timber.d("File Size= $contentLength")
                        inputStream = body.byteStream()
                        outputStream = file.outputStream() /*encryptedFile.openFileOutput()*/
                        val data = ByteArray(4096)
                        var count: Int
                        var progress = 0
                        while (inputStream.read(data).also { count = it } != -1) {
                            outputStream.write(data, 0, count)
                            progress += count
                            val finalPercentage = (progress * 100 / contentLength)
                            emit(DownloadFileResult.Progress(finalPercentage))
                            Timber.d(
                                "Progress: $progress/${contentLength} >>>> $finalPercentage"
                            )
                        }
                        outputStream.flush()
                        Timber.d("File saved successfully!")
                        emit(DownloadFileResult.Success(fileName))
                    } catch (e: IOException) {
                        Timber.e(e)
                        emit(DownloadFileResult.Error("Failed to save the file!", null))
                    } finally {
                        inputStream?.close()
                        outputStream?.close()
                    }
                } catch (e: IOException) {
                    Timber.e(e)
                    emit(DownloadFileResult.Error("Failed to save the file!", null))
                }
            } else {
                emit(DownloadFileResult.Error("Error al descargar putencio"))
            }
        }
    }
}