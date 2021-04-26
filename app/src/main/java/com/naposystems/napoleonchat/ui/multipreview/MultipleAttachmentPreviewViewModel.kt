package com.naposystems.napoleonchat.ui.multipreview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.service.multiattachment.MultipleUploadService
import com.naposystems.napoleonchat.service.multiattachment.MultipleUploadService.Companion.ATTACHMENT_KEY
import com.naposystems.napoleonchat.service.multiattachment.MultipleUploadService.Companion.MESSAGE_KEY
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.ui.conversation.model.ItemMessage
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.multipreview.contract.IContractMultipleAttachmentPreview
import com.naposystems.napoleonchat.ui.multipreview.events.MultipleAttachmentPreviewAction
import com.naposystems.napoleonchat.ui.multipreview.events.MultipleAttachmentPreviewAction.SelectItemInTabLayout
import com.naposystems.napoleonchat.ui.multipreview.events.MultipleAttachmentPreviewAction.ShowSelfDestruction
import com.naposystems.napoleonchat.ui.multipreview.events.MultipleAttachmentPreviewMode
import com.naposystems.napoleonchat.ui.multipreview.events.MultipleAttachmentPreviewState
import com.naposystems.napoleonchat.ui.previewMedia.IContractPreviewMedia
import com.naposystems.napoleonchat.ui.selfDestructTime.IContractSelfDestructTime
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SingleLiveEvent
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.extensions.isVideo
import kotlinx.coroutines.launch
import javax.inject.Inject

class MultipleAttachmentPreviewViewModel @Inject constructor(
    private val repository: IContractSelfDestructTime.Repository,
    private val repositoryMessages: IContractMultipleAttachmentPreview.Repository,
    private val repositoryPreviewMedia: IContractPreviewMedia.Repository,
    private val context: Context
) : ViewModel(),
    IContractMultipleAttachmentPreview.ViewModel,
    LifecycleObserver {

    private var isShowingOptions = true
    private var listFiles = mutableListOf<MultipleAttachmentFileItem>()
    private var contact: ContactEntity? = null
    private var modeOnlyView: Boolean = false

    private val _state = MutableLiveData<MultipleAttachmentPreviewState>()
    val state: LiveData<MultipleAttachmentPreviewState>
        get() = _state

    private val actions: SingleLiveEvent<MultipleAttachmentPreviewAction> = SingleLiveEvent()
    fun actions(): LiveData<MultipleAttachmentPreviewAction> = actions

    private val modes: SingleLiveEvent<MultipleAttachmentPreviewMode> = SingleLiveEvent()
    fun modes(): LiveData<MultipleAttachmentPreviewMode> = modes

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initUi() {
        loading()
    }

    private fun loading() {
        _state.value = MultipleAttachmentPreviewState.Loading
    }

    fun changeVisibilityOptions() {
        isShowingOptions = isShowingOptions.not()
        if (isShowingOptions) {
            actions.value = MultipleAttachmentPreviewAction.ShowAttachmentOptions
        } else {
            actions.value = MultipleAttachmentPreviewAction.HideAttachmentOptions
        }
    }

    fun defineListFiles(files: ArrayList<MultipleAttachmentFileItem>) {
        /**
         * Cuando el video es local, osea soy el que envia, no necesitamos nada adicional, pero si
         * el video es descargado, osea soy el receptor, debemos crear el archivo temporal ó generar
         * el uri segun sea el caso
         */
        val filesWithoutUri = files.filter { it.contentUri == null }
        if (filesWithoutUri.isEmpty()) {
            listFiles = files
            defineDefaultSelfDestructionTime()
            showFilesAsPager()
        } else {
            createUriForFiles(filesWithoutUri)
        }
    }

    private fun createUriForFiles(filesWithoutUri: List<MultipleAttachmentFileItem>) {
        viewModelScope.launch {
            filesWithoutUri.forEach { file ->
                if (file.isVideo()) {
                    if (BuildConfig.ENCRYPT_API) {
                        val attachmentEntity =
                            file.messageAndAttachment?.attachment?.toAttachmentEntity()
                        attachmentEntity?.let { attachment ->
                            val newUri = repositoryPreviewMedia.createTempFile(attachment)
                            newUri?.let { file.contentUri = it.toUri() }
                        }
                    } else {
                        file.contentUri = Utils.getFileUri(
                            context = context,
                            subFolder = Constants.CacheDirectories.VIDEOS.folder,
                            fileName = file.messageAndAttachment?.attachment?.fileName ?: ""
                        )
                    }
                }
            }

            listFiles = filesWithoutUri.toMutableList()
            defineDefaultSelfDestructionTime()
            showFilesAsPager()
        }
    }

    fun forceShowOptions() {
        isShowingOptions = true
        actions.value = MultipleAttachmentPreviewAction.ShowAttachmentOptionsWithoutAnim
    }

    fun updateSelfDestructionForItemPosition(
        selectedFileToSee: Int,
        selfDestructTimeSelected: Int
    ) {
        val file = listFiles[selectedFileToSee]
        listFiles.find { it.id == file.id }?.let {
            it.selfDestruction = selfDestructTimeSelected
        }
    }

    fun onDeleteElementInCreating(selectedIndexToDelete: Int) {
        removeFileFromListAndShowListInPager(selectedIndexToDelete)
        if (isTheLastFile()) {
            exitPreview()
        } else {
            selectItemInTabLayoutByIndex(selectedIndexToDelete)
        }
    }

    fun loadSelfDestructionTimeByIndex(position: Int) {
        if (modeOnlyView.not()) {
            actions.value = ShowSelfDestruction(listFiles[position].selfDestruction)
        }
    }

    fun validateMustMarkAsReaded(position: Int) {
        viewModelScope.launch {
            val attachment = listFiles[position].messageAndAttachment?.attachment
            attachment?.let {
                val msgAttachment = listFiles[position].messageAndAttachment
                msgAttachment?.let {
                    if (it.isRead.not() && it.isMine == Constants.IsMine.NO.value) {
                        it.isRead = repositoryPreviewMedia.sentAttachmentAsRead(
                            it.attachment,
                            it.contactId
                        )
                    }
                }
            }
        }
    }

    fun setContact(contactEntity: ContactEntity) {
        contact = contactEntity
    }

    private fun showFilesAsPager() {
        _state.value = MultipleAttachmentPreviewState.SuccessFilesAsPager(ArrayList(listFiles))
        validateMustShowTabs()
    }

    private fun exitPreview() {
        actions.value = MultipleAttachmentPreviewAction.Exit
    }

    private fun isTheLastFile(): Boolean = listFiles.isEmpty()

    private fun validateMustShowTabs() {
        if (listFiles.size == 1) {
            actions.value = MultipleAttachmentPreviewAction.HideFileTabs
        }
    }

    private fun selectItemInTabLayoutByIndex(selectedIndexToDelete: Int) {
        val indexToSelectInTabLayout =
            if (selectedIndexToDelete == 0) 0 else selectedIndexToDelete - 1
        actions.value = SelectItemInTabLayout(indexToSelectInTabLayout)
    }

    private fun removeFileFromListAndShowListInPager(selectedIndexToDelete: Int) {
        val file = listFiles[selectedIndexToDelete]
        listFiles.remove(file)
        showFilesAsPager()
    }

    private fun defineDefaultSelfDestructionTime() {
        contact?.let {
            viewModelScope.launch {
                val selfDestructionTime =
                    repository.getSelfDestructTimeAsIntByContact(contactId = it.id)
                listFiles.forEach {
                    it.selfDestruction = selfDestructionTime
                }
            }
        }
    }

    fun saveMessageAndAttachments(message: String) {
        loading()
        val itemMessage = getItemMessageToSend(message)
        viewModelScope.launch {
            try {
                contact?.let {
                    repositoryMessages.apply {
                        val messageEntity = insertMessageToContact(itemMessage)
                        deleteMessageNotSent(it.id)
                        val attachments = insertAttachmentsWithMsgId(listFiles, messageEntity.id)
                        actions.value = MultipleAttachmentPreviewAction.SendMessageToRemote(
                            messageEntity,
                            attachments
                        )
                    }
                }
            } catch (exception: Exception) {
                actions.value = MultipleAttachmentPreviewAction.Exit
            }
        }
    }

    private fun setMessageWebIdToAttachments(
        attachments: List<AttachmentEntity?>,
        messageResponse: Pair<MessageEntity?, String>?
    ): List<AttachmentEntity?> {
        attachments.forEach { attachment ->
            attachment?.let { it.messageWebId = messageResponse?.second ?: "" }
        }
        return attachments
    }

    private fun getItemMessageToSend(message: String): ItemMessage {
        return ItemMessage(
            messageString = message,
            attachment = null,
            numberAttachments = listFiles.size,
            selfDestructTime = getHighestTimeInFiles(),
            quote = "",
            contact = contact
        )
    }

    private fun getHighestTimeInFiles(): Int = listFiles.maxOfOrNull { it.selfDestruction } ?: 0

    fun sendMessageToRemote(
        messageEntity: MessageEntity,
        attachments: List<AttachmentEntity?>
    ) {
        if (messageEntity.mustSendToRemote()) {
            viewModelScope.launch {
                val messageResponse = repositoryMessages.sendMessage(messageEntity)
                val attachmentsWithWebId =
                    setMessageWebIdToAttachments(attachments, messageResponse)
                messageResponse?.let { pairData ->
                    pairData.first?.let {
                        sendMessageToRemote(it, attachmentsWithWebId)
                    }
                }
            }
        } else {
            initUploadServiceForSendFiles(messageEntity, attachments)
        }
    }

    private fun initUploadServiceForSendFiles(
        messageEntity: MessageEntity,
        attachments: List<AttachmentEntity?>
    ) {
        // we can create notification for upload attachments
        // todo: mover esto a un activity para usar el context
        val intent = Intent(context, MultipleUploadService::class.java).apply {
            putExtras(Bundle().apply {
                putParcelable(MESSAGE_KEY, messageEntity)
                putParcelableArrayList(ATTACHMENT_KEY, ArrayList(attachments))
            })
        }
        context.startService(intent)
        actions.value = MultipleAttachmentPreviewAction.ExitToConversation
    }

    fun defineModeOnlyViewInConversation(modeOnlyView: Boolean, message: String?) {
        this.modeOnlyView = modeOnlyView
        if (modeOnlyView) {
            message?.let { modes.value = MultipleAttachmentPreviewMode.ModeView(it) }
        } else {
            modes.value = MultipleAttachmentPreviewMode.ModeCreate
        }
    }

    fun onDeleteAttachment() {
        val msgAndAttach = listFiles[0].messageAndAttachment
        msgAndAttach?.let {
            if (it.isMine == Constants.IsMine.NO.value) {
                actions.value = MultipleAttachmentPreviewAction.RemoveAttachForReceiver
            } else {
                actions.value = MultipleAttachmentPreviewAction.RemoveAttachForSender
            }
        } ?: run {
            actions.value = MultipleAttachmentPreviewAction.RemoveAttachInCreate
        }
    }

}