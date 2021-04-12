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

        /**
         * Load files from folder, by the folder id
         *
         * @param folder the folder identifier
         *
         */
        fun loadFilesFromFolder(folder: MultipleAttachmentFolderItem)

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
        fun getFilesByFolder(
            folderParent: String,
            mapIds: Map<Int, Int>
        ): Flow<MultipleAttachmentState>

    }

    interface MediaStore {

        /**
         * Get folders for the conversation, this is a old implementation
         *
         * @return list of MultipleAttachmentFolderItem (folders)
         */
        fun getFoldersForConversation(): List<MultipleAttachmentFolderItem>

        fun getFilesByFolder(
            folderParent: String,
            mapIds: Map<Int, Int>
        ): List<MultipleAttachmentFileItem>

    }

}