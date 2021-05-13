package com.naposystems.napoleonchat.ui.multipreview.viewmodels

import androidx.lifecycle.*
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.multipreview.contract.IContractMultipleAttachmentItemPreview
import javax.inject.Inject

class MultipleAttachmentPreviewItemViewModel @Inject constructor(
    private val repository: IContractMultipleAttachmentItemPreview.Repository,
) : ViewModel() {

    private lateinit var _attachment: LiveData<AttachmentEntity?>
    val attachment: LiveData<AttachmentEntity?>
        get() = _attachment

    fun setAttachmentAndLaunchLiveData(attachmentWebId: String) {
        _attachment = repository.getAttachmentLiveData(attachmentWebId)
    }

}