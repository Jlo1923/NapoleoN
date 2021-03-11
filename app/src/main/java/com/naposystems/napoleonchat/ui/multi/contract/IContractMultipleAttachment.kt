package com.naposystems.napoleonchat.ui.multi.contract

import com.naposystems.napoleonchat.ui.multi.events.MultipleAttachmentState
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFolderItem
import kotlinx.coroutines.flow.Flow

interface IContractMultipleAttachment {

    interface ViewModel {

        /**
         * Get the folders to show the user
         */
        fun getFolders()

        fun addFileToList(item: MultipleAttachmentFileItem)

        fun removeFileToList(item: MultipleAttachmentFileItem)
    }

    interface Repository {

        /**
         * Get folders for the user
         */
        fun getFolders(): Flow<MultipleAttachmentState>

        /**
         * get Files from a folder Name
         */
        fun getFilesByFolder(folderName: String): Flow<MultipleAttachmentState>

    }

    interface MediaStore {

        fun getFoldersForConversation(): List<MultipleAttachmentFolderItem>

        fun getFilesByFolder(folderName: String): List<MultipleAttachmentFileItem>

    }

}