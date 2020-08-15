package com.naposystems.napoleonchat.ui.previewMedia

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment
import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import com.naposystems.napoleonchat.utility.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class PreviewMediaViewModel @Inject constructor(private val repository: IContractPreviewMedia.Repository) :
    ViewModel(), IContractPreviewMedia.ViewModel {

    private val _tempFile = MutableLiveData<File>()
    val tempFile: LiveData<File>
        get() = _tempFile

    //region Implementation IContractPreviewMedia.ViewModel
    override fun createTempFile(attachment: Attachment) {
        viewModelScope.launch {
            _tempFile.value = repository.createTempFile(attachment)
        }
    }

    override fun sentMessageReaded(messageAndAttachment: MessageAndAttachment) {
        CoroutineScope(Dispatchers.IO).launch {
            if (messageAndAttachment.message.isMine == Constants.IsMine.NO.value) {
                repository.sentMessageReaded(messageAndAttachment)
            }
        }
    }

    //endregion
}