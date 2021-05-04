package com.naposystems.napoleonchat.ui.multipreview.repository

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.datasource.attachment.AttachmentLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.multipreview.contract.IContractMultipleAttachmentItemPreview
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MultipleAttachmentPreviewItemRepository @Inject constructor(
    private val attachmentLocalDataSource: AttachmentLocalDataSource
) : IContractMultipleAttachmentItemPreview.Repository {

    override fun getAttachmentLiveData(
        attachmentWebId: String
    ): LiveData<AttachmentEntity?> {
        return attachmentLocalDataSource.getAttachmentByWebIdLiveData(attachmentWebId)
    }

}