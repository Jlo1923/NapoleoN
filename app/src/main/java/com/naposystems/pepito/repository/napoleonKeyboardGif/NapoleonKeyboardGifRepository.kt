package com.naposystems.pepito.repository.napoleonKeyboardGif

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.naposystems.pepito.ui.napoleonKeyboardGif.IContractNapoleonKeyboardGif
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.FileManager
import com.naposystems.pepito.webService.NapoleonApi
import javax.inject.Inject

class NapoleonKeyboardGifRepository @Inject constructor(
    private val context: Context,
    private val napoleonApi: NapoleonApi
) :
    IContractNapoleonKeyboardGif.Repository {

    override suspend fun downloadGif(
        url: String,
        progressLiveData: MutableLiveData<Float>
    ): String {
        val responseDownloadFile =
            napoleonApi.downloadFileByUrl(url)

        return if (responseDownloadFile.isSuccessful) {
            FileManager.saveToDisk(
                context = context,
                body = responseDownloadFile.body()!!,
                type = Constants.AttachmentType.GIF.type,
                extension = "gif",
                progressLiveData = progressLiveData
            )
        } else {
            ""
        }
    }
}