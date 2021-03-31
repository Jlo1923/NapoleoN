package com.naposystems.napoleonchat.ui.previewmulti

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.*
import com.naposystems.napoleonchat.service.multiattachment.MultipleUploadService
import com.naposystems.napoleonchat.service.multiattachment.MultipleUploadService.Companion.ATTACHMENT_KEY
import com.naposystems.napoleonchat.service.multiattachment.MultipleUploadService.Companion.MESSAGE_KEY
import com.naposystems.napoleonchat.service.uploadService.UploadService
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.ui.conversation.model.ItemMessage
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.previewmulti.contract.IContractMultipleAttachmentPreview
import com.naposystems.napoleonchat.ui.previewmulti.events.MultipleAttachmentPreviewAction
import com.naposystems.napoleonchat.ui.previewmulti.events.MultipleAttachmentPreviewAction.SelectItemInTabLayout
import com.naposystems.napoleonchat.ui.previewmulti.events.MultipleAttachmentPreviewAction.ShowSelfDestruction
import com.naposystems.napoleonchat.ui.previewmulti.events.MultipleAttachmentPreviewState
import com.naposystems.napoleonchat.ui.selfDestructTime.IContractSelfDestructTime
import com.naposystems.napoleonchat.utility.SingleLiveEvent
import kotlinx.coroutines.launch
import javax.inject.Inject

class MultipleAttachmentPreviewViewModel @Inject constructor(
    private val repository: IContractSelfDestructTime.Repository,
    private val repositoryMessages: IContractMultipleAttachmentPreview.Repository,
    private val context: Context
) : ViewModel(),
    IContractMultipleAttachmentPreview.ViewModel,
    LifecycleObserver {

    private var isShowingOptions = true
    private var listFiles = mutableListOf<MultipleAttachmentFileItem>()
    private var contact: ContactEntity? = null

    private val _state = MutableLiveData<MultipleAttachmentPreviewState>()
    val state: LiveData<MultipleAttachmentPreviewState>
        get() = _state

    private val actions: SingleLiveEvent<MultipleAttachmentPreviewAction> = SingleLiveEvent()
    fun actions(): LiveData<MultipleAttachmentPreviewAction> = actions

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
        listFiles = files
        defineDefaultSelfDestructionTime()
        showFilesAsPager()
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

    fun onDeleteElement(selectedIndexToDelete: Int) {
        removeFileFromListAndShowListInPager(selectedIndexToDelete)
        if (isTheLastFile()) {
            exitPreview()
        } else {
            selectItemInTabLayoutByIndex(selectedIndexToDelete)
        }
    }

    fun loadSelfDestructionTimeByIndex(position: Int) {
        actions.value =
            ShowSelfDestruction(listFiles[position].selfDestruction)
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
        val selfDestructionTime = repository.getSelfDestructTime()
        listFiles.forEach {
            it.selfDestruction = selfDestructionTime
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
                        sendMessageAndAttachmentsToRemote(messageEntity, attachments)
                    }
                }
            } catch (exception: Exception) {

            }
        }
    }

    private suspend fun sendMessageAndAttachmentsToRemote(
        messageEntity: MessageEntity,
        attachments: List<AttachmentEntity?>
    ) {
        if (messageEntity.mustSendToRemote()) {
            val messageResponse = repositoryMessages.sendMessage(messageEntity)
            val attachmentsWithWebId = setMessageWebIdToAttachments(attachments, messageResponse)
            messageResponse?.let { pairData ->
                pairData.first?.let {
                    sendMessageAndAttachmentsToRemote(it, attachmentsWithWebId)
                }
            }
        } else {
            // we can create notification for upload attachments
            // todo: mover esto a un activity para usar el context
            val intent = Intent(context, MultipleUploadService::class.java).apply {
                putExtras(Bundle().apply {
                    putParcelable(MESSAGE_KEY, messageEntity)
                    putParcelableArrayList(ATTACHMENT_KEY, ArrayList(attachments))
                })
            }
            context.startService(intent)
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

}