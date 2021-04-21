package com.naposystems.napoleonchat.service.download.contract

import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity

interface IContractDownloadService {

    interface Service {

        /**
         * Allows you to tell the system to create the notification to inform the user
         * of the download status
         */
        fun showNotification()

        /**
         * Allows you to subscribe to the different events that a download can generate
         */
        fun subscribeRxEvents()
    }

    interface Repository {

        /**
         * This method allows you to take a list of attachments and start their respective download,
         * at the end of one it will try to take the next.
         *
         * @param listAttachments the attachments
         */
        fun downloadAttachment(attachmentEntity: AttachmentEntity)

        /**
         * allows you to cancel the download in progress
         */
        fun cancelDownload()

        /**
         * Allows you to update the status of an attachment
         *
         * @param attachmentEntity the attachment
         */
        fun updateAttachment(attachmentEntity: AttachmentEntity)

    }
}