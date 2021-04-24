package com.naposystems.napoleonchat.ui.multi.contract

import com.naposystems.napoleonchat.ui.multi.events.MultipleAttachmentState
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFolderItem
import kotlinx.coroutines.flow.Flow

interface IContractMultipleAttachment {

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
            folderName: String? = null,
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
            folderName: String? = null,
            mapIds: Map<Int, Int>
        ): List<MultipleAttachmentFileItem>

    }

}