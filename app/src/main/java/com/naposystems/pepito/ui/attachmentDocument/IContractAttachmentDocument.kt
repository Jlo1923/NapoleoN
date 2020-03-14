package com.naposystems.pepito.ui.attachmentDocument

interface IContractAttachmentDocument {

    interface ViewModel {
        fun getDocumentsFromMediaStore()
    }

    interface Repository {
        fun queryDocuments()
    }
}