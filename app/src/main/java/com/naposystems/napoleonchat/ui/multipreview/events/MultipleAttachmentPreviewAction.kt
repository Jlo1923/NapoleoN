package com.naposystems.napoleonchat.ui.multipreview.events

import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageEntity

sealed class MultipleAttachmentPreviewAction {

    object Exit : MultipleAttachmentPreviewAction()
    object ExitToConversation : MultipleAttachmentPreviewAction()
    object ShowAttachmentOptions : MultipleAttachmentPreviewAction()
    object ShowAttachmentOptionsWithoutAnim : MultipleAttachmentPreviewAction()
    object HideAttachmentOptions : MultipleAttachmentPreviewAction()
    object HideFileTabs : MultipleAttachmentPreviewAction()

    class ShowSelectFolderName(
        val folderName: String
    ) : MultipleAttachmentPreviewAction()

    class SelectItemInTabLayout(
        val indexItem: Int
    ) : MultipleAttachmentPreviewAction()

    class ShowSelfDestruction(
        val selfDestruction: Int
    ) : MultipleAttachmentPreviewAction()

    class SendMessageToRemote(
        val messageEntity: MessageEntity,
        val attachments: List<AttachmentEntity?>
    ) : MultipleAttachmentPreviewAction()

}