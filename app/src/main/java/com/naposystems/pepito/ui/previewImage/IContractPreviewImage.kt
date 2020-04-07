package com.naposystems.pepito.ui.previewImage

import android.content.Context
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.User

interface IContractPreviewImage {

    interface ViewModel {
        fun setContact(context : Context, contact: Contact)
        fun setUser(context : Context, user: User)
        fun resetImage()
    }

}