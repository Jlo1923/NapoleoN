package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.contract

import android.content.Context
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity

interface IContractIncomingMultiAttachmentMsg {

    interface ViewModel {

        fun validateStatusAndQuantity(listAttachments: List<AttachmentEntity>)

        fun retryDownloadAllFiles(attachmentsFilter: List<AttachmentEntity>, context: Context)

        fun cancelDownload(attachmentEntity: AttachmentEntity)

        fun retryDownload(attachmentEntity: AttachmentEntity, context: Context)

    }


    interface Repository {

    }
}