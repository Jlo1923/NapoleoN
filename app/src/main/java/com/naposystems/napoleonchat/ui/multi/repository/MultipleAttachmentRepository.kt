package com.naposystems.napoleonchat.ui.multi.repository

import com.naposystems.napoleonchat.ui.multi.contract.IContractMultipleAttachment
import com.naposystems.napoleonchat.ui.multi.events.MultipleAttachmentState
import com.naposystems.napoleonchat.ui.multi.views.itemview.MultipleAttachmentFileItemView
import com.naposystems.napoleonchat.ui.multi.views.itemview.MultipleAttachmentFolderItemView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MultipleAttachmentRepository @Inject constructor(
    private val mediaStore: IContractMultipleAttachment.MediaStore
) : IContractMultipleAttachment.Repository {

    override fun getFolders(): Flow<MultipleAttachmentState> = flow {
        withContext(Dispatchers.IO) {
            try {
                emit(MultipleAttachmentState.Loading)
                val folders = mediaStore.getFoldersForConversation()
                val items = folders.map { MultipleAttachmentFolderItemView(it) }
                emit(MultipleAttachmentState.SuccessFolders(items))
            } catch (exception: Exception) {
                emit(MultipleAttachmentState.Error)
            }
        }
    }

    override fun getFilesByFolder(
        folderParent: String,
        folderName: String?,
        mapIds: Map<Int, Int>
    ): Flow<MultipleAttachmentState> = flow {
        withContext(Dispatchers.IO) {
            try {
                emit(MultipleAttachmentState.Loading)
                val files = mediaStore.getFilesByFolder(folderParent, folderName, mapIds)
                val items = files.map { MultipleAttachmentFileItemView(it) }
                emit(MultipleAttachmentState.SuccessFiles(items))
            } catch (exception: Exception) {
                emit(MultipleAttachmentState.Error)
            }
        }
    }

}