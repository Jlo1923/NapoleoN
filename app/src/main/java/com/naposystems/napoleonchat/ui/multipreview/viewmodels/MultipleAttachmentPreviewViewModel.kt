package com.naposystems.napoleonchat.ui.multipreview.viewmodels

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
import com.naposystems.napoleonchat.source.remote.dto.conversation.deleteMessages.DeleteMessagesReqDTO
import com.naposystems.napoleonchat.ui.conversation.model.ItemMessage
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.multipreview.contract.IContractMultipleAttachmentPreview
import com.naposystems.napoleonchat.ui.multipreview.events.MultipleAttachmentPreviewAction
import com.naposystems.napoleonchat.ui.multipreview.events.MultipleAttachmentPreviewAction.ExitToConversationAndSendData
import com.naposystems.napoleonchat.ui.multipreview.events.MultipleAttachmentPreviewMode
import com.naposystems.napoleonchat.ui.multipreview.events.MultipleAttachmentPreviewState
import com.naposystems.napoleonchat.ui.multipreview.events.MultipleAttachmentPreviewState.SuccessFilesAsPager
import com.naposystems.napoleonchat.ui.previewMedia.IContractPreviewMedia
import com.naposystems.napoleonchat.dialog.selfDestructTime.SelfDestructTimeDialogRepository
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SingleLiveEvent
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.extensions.isVideo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class MultipleAttachmentPreviewViewModel @Inject constructor(
    private val repository: SelfDestructTimeDialogRepository,
    private val repositoryMessages: IContractMultipleAttachmentPreview.Repository,
    private val repositoryPreviewMedia: IContractPreviewMedia.Repository,
    private val context: Context
) : ViewModel(),
    IContractMultipleAttachmentPreview.ViewModel,
    LifecycleObserver {

    private var isShowingOptions = true
    private var listFiles = mutableListOf<MultipleAttachmentFileItem>()
    private var listFilesForRemoveInCreate = mutableListOf<MultipleAttachmentFileItem>()
    private var contactEntity: ContactEntity? = null
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

    fun changeVisibilityOptions() {
        isShowingOptions = isShowingOptions.not()
        actions.value = if (isShowingOptions) {
            MultipleAttachmentPreviewAction.ShowAttachmentOptions
        } else {
            MultipleAttachmentPreviewAction.HideAttachmentOptions
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
        } else {
            createUriForFiles(filesWithoutUri)
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
            //selectItemInTabLayoutByIndex(selectedIndexToDelete)
        }
    }

    fun loadSelfDestructionTimeByIndex(position: Int) {
        if (modeOnlyView.not()) {
            actions.value =
                MultipleAttachmentPreviewAction.ShowSelfDestruction(listFiles[position].selfDestruction)
        }
    }

    fun validateMustAttachmentMarkAsReaded(position: Int) {
        GlobalScope.launch {
            if (listFiles.isNotEmpty()) {
                if (listFiles[position].isVideo().not()) {
                    markAttachmentImageAsRead(position)
                }
            }
        }
    }

    private suspend fun markAttachmentImageAsRead(position: Int) {
        val attachment = listFiles[position].messageAndAttachment?.attachment
        attachment?.let { attachment ->
            val msgAttachment = listFiles[position].messageAndAttachment
            msgAttachment?.let { itemMessage ->
                if (attachment.status != Constants.AttachmentStatus.READED.status
                    && itemMessage.isMine == Constants.IsMine.NO.value
                ) {
                    itemMessage.isRead = repositoryPreviewMedia.sentAttachmentAsRead(
                        itemMessage.attachment,
                        itemMessage.contactId
                    )
                }
                repository.tryMarkMessageParentAsRead(attachment.webId)
            }
        }
    }

    fun setContact(contactEntity: ContactEntity) {
        this.contactEntity = contactEntity
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

    fun saveMessageAndAttachments(message: String) {
        loading()
        val itemMessage = getItemMessageToSend(message)
        viewModelScope.launch {
            try {
                contactEntity?.let {
                    repositoryMessages.apply {
                        val messageEntity = insertMessageToContact(itemMessage)
                        deleteMessageNotSent(it.id)
                        val attachments = insertAttachmentsWithMsgId(listFiles, messageEntity.id)
                        actions.value = ExitToConversationAndSendData(messageEntity, attachments)
                    }
                }
            } catch (exception: Exception) {
                exitPreview()
            }
        }
    }

    fun markAttachmentVideoAsRead(fileItem: MultipleAttachmentFileItem) {
        GlobalScope.launch {
            fileItem.messageAndAttachment?.let {
                if (it.isMine == Constants.IsMine.NO.value) {
                    repository.sentAttachmentReaded(fileItem)
                }
            }
        }
    }

    fun onDeleteAttachmentForUser(selectedIndexToDelete: Int) {
        viewModelScope.launch {
            val file = listFiles[selectedIndexToDelete]
            file.messageAndAttachment?.let {
                val isDelete = repository.deleteAttachmentLocally(it.attachment.webId)
                if (isDelete) {
                    removeFileFromListAndShowListInPager(selectedIndexToDelete)
                }
            }
        }
    }

    fun onDeleteAttachmentForAll(selectedIndexToDelete: Int) {
        viewModelScope.launch {
            val file = listFiles[selectedIndexToDelete]
            contactEntity?.let { contact ->
                loading()
                file.messageAndAttachment?.let {
                    val isDelete = repository.deleteAttachmentLocally(it.attachment.webId)
                    if (isDelete) {
                        val contactId = contact.id
                        val objectForDelete = DeleteMessagesReqDTO(
                            userReceiver = contactId,
                            attachmentsId = listOf(it.attachment.webId)
                        )
                        val response = repository.deleteMessagesForAll(objectForDelete)
                        if (response.isSuccessful) {
                            removeFileFromListAndShowListInPager(selectedIndexToDelete)
                        }
                    }
                }
            }
        }
    }

    fun deleteAttachmentByDestructionTime(webId: String, position: Int) {
        viewModelScope.launch {
            val isDelete = repository.deleteAttachmentLocally(webId)
            if (isDelete) {
                removeFileFromListAndShowListInPager(position)
            }
        }
    }

    fun validateExitInCreateMode() {
        if (listFilesForRemoveInCreate.isEmpty()) {
            exitPreview()
        } else {
            repository.saveDeleteFilesInCache(listFilesForRemoveInCreate.toList())
            actions.value =
                MultipleAttachmentPreviewAction.ExitAndSendDeleteFiles(listFilesForRemoveInCreate.toList())
        }
    }

    fun onChangeSelfDestruction(iconSelfDestruction: Int) = contactEntity?.let {
        actions.value =
            MultipleAttachmentPreviewAction.OnChangeSelfDestruction(it.id, iconSelfDestruction)
    }

    private fun showFilesAsPager(indexToSelect: Int? = null) {
        if (listFiles.isNotEmpty()) {
            val indexToSelectInPager =
                indexToSelect?.let { if (it == 0) 0 else it - 1 } ?: run { null }
            _state.value = SuccessFilesAsPager(ArrayList(listFiles), indexToSelectInPager)
            validateMustShowTabs()
        } else {
            exitPreview()
        }
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
        actions.value =
            MultipleAttachmentPreviewAction.SelectItemInTabLayout(indexToSelectInTabLayout)
    }

    private fun removeFileFromListAndShowListInPager(selectedIndexToDelete: Int) {
        if (selectedIndexToDelete < listFiles.size) {
            val file = listFiles[selectedIndexToDelete]
            listFilesForRemoveInCreate.add(file)
            listFiles.remove(file)
            showFilesAsPager(selectedIndexToDelete)
        }
    }

    private fun defineDefaultSelfDestructionTime() {
        contactEntity?.let {
            viewModelScope.launch {
                val selfDestructionTime = repository.getSelfDestructTimeAsIntByContact(it.id)
                val selfDestructionTimeFinal = if (selfDestructionTime < 0) {
                    repository.getSelfDestructTime()
                } else {
                    selfDestructionTime
                }
                listFiles.forEach { it.selfDestruction = selfDestructionTimeFinal }
                showFilesAsPager()
            }
        }
    }

    private fun setMessageWebIdToAttachments(
        attachments: List<AttachmentEntity?>,
        messageResponse: Pair<MessageEntity?, String>?
    ): List<AttachmentEntity?> {
        attachments.forEach { attachment ->
            attachment?.let {
                it.messageWebId = messageResponse?.second ?: ""
            }
        }
        return attachments
    }

    private fun getItemMessageToSend(message: String): ItemMessage = ItemMessage(
        messageString = message,
        attachment = null,
        numberAttachments = listFiles.size,
        selfDestructTime = getHighestTimeInFiles(),
        quote = "",
        contact = contactEntity
    )

    private fun getHighestTimeInFiles(): Int = listFiles.maxOfOrNull { it.selfDestruction } ?: 0

    private fun loading() {
        _state.value = MultipleAttachmentPreviewState.Loading
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

    fun markWasInPreviewActivity() {
        repository.markWasInPreviewActivity()
    }


}