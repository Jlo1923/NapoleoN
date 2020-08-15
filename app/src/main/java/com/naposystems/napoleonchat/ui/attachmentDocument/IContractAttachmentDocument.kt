package com.naposystems.napoleonchat.ui.attachmentDocument

interface IContractAttachmentDocument {

    interface ViewModel {
        fun getDocumentsFromMediaStore()
    }

    interface Repository {
        fun queryDocuments()
    }
}