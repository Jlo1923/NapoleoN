package com.naposystems.napoleonchat.ui.previewMedia

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.utility.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class PreviewMediaViewModel @Inject constructor(
    private val repository: IContractPreviewMedia.Repository
) : ViewModel(), IContractPreviewMedia.ViewModel {

    private val _tempFile = MutableLiveData<File>()
    val tempFile: LiveData<File>
        get() = _tempFile

    //region Implementation IContractPreviewMedia.ViewModel
    override fun createTempFile(attachmentEntity: AttachmentEntity) {
        viewModelScope.launch {
            _tempFile.value = repository.createTempFile(attachmentEntity)
        }
    }

    override fun sentMessageReaded(messageAndAttachmentRelation: MessageAttachmentRelation) {
        CoroutineScope(Dispatchers.IO).launch {
            if (messageAndAttachmentRelation.messageEntity.isMine == Constants.IsMine.NO.value) {
                repository.sentMessageReaded(messageAndAttachmentRelation)
            }
        }
    }

    //endregion
}