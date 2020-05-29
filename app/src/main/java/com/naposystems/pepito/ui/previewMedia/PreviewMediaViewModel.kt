package com.naposystems.pepito.ui.previewMedia

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.entity.message.attachments.Attachment
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
    //endregion
}