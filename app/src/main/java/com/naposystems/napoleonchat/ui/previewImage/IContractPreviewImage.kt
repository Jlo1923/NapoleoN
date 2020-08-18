package com.naposystems.napoleonchat.ui.previewImage

import android.content.Context
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.entity.User

interface IContractPreviewImage {

    interface ViewModel {
        fun setContact(context : Context, contact: Contact)
        fun setUser(context : Context, user: User)
        fun resetImage()
    }

}