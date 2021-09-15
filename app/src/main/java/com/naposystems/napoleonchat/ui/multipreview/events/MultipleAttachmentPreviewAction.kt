package com.naposystems.napoleonchat.ui.multipreview.events

import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem

sealed class MultipleAttachmentPreviewAction {

    object Exit : MultipleAttachmentPreviewAction()
    object ExitToConversation : MultipleAttachmentPreviewAction()
    object ShowAttachmentOptions : MultipleAttachmentPreviewAction()
    object ShowAttachmentOptionsWithoutAnim : MultipleAttachmentPreviewAction()
    object HideAttachmentOptions : MultipleAttachmentPreviewAction()
    object HideFileTabs : MultipleAttachmentPreviewAction()
    object RemoveAttachInCreate : MultipleAttachmentPreviewAction()
    object RemoveAttachForReceiver : MultipleAttachmentPreviewAction()
    object RemoveAttachForSender : MultipleAttachmentPreviewAction()

    class SelectItemInTabLayout(
        val indexItem: Int
    ) : MultipleAttachmentPreviewAction()

    class ShowSelfDestruction(
        val selfDestruction: Int
    ) : MultipleAttachmentPreviewAction()

    class ExitToConversationAndSendData(
        val messageEntity: MessageEntity,
        val attachments: List<AttachmentEntity?>
    ) : MultipleAttachmentPreviewAction()

    class ExitAndSendDeleteFiles(
        val listFilesForRemoveInCreate: List<MultipleAttachmentFileItem>
    ) : MultipleAttachmentPreviewAction()

    class OnChangeSelfDestruction(
        val contactId: Int,
        val iconSelfDestruction: Int
    ) : MultipleAttachmentPreviewAction()

    class ShowUpload(
        val shouldShowUpload: Boolean
    ) : MultipleAttachmentPreviewAction()
}