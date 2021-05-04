package com.naposystems.napoleonchat.ui.multipreview.contract

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.ui.conversation.model.ItemMessage
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import kotlinx.coroutines.flow.Flow

interface IContractMultipleAttachmentItemPreview {

    interface Repository {

        /**
         * get the attachment info with livedata
         *
         * @param attachmentId: the attachment id
         * @return Livedata from the attachment
         */
        fun getAttachmentLiveData(
            attachmentWebId: String
        ): LiveData<AttachmentEntity?>

    }


}