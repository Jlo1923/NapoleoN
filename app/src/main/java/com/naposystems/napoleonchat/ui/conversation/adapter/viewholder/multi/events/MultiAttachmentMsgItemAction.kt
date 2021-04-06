package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events

import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity

sealed class MultiAttachmentMsgItemAction {

    class CancelUpload(
        val attachmentEntity: AttachmentEntity
    ) : MultiAttachmentMsgItemAction()

    class CancelDownload(
        val attachmentEntity: AttachmentEntity
    ) : MultiAttachmentMsgItemAction()

    class RetryUpload(
        val attachmentEntity: AttachmentEntity
    ) : MultiAttachmentMsgItemAction()

    class RetryDownload(
        val attachmentEntity: AttachmentEntity
    ) : MultiAttachmentMsgItemAction()

    class ViewAttachment(
        val attachmentEntity: AttachmentEntity,
        val index: Int
    ) : MultiAttachmentMsgItemAction()

}