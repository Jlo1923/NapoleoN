package com.naposystems.napoleonchat.ui.previewImage

import android.content.Context
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.UserEntity

interface IContractPreviewImage {

    interface ViewModel {
        fun setContact(context : Context, contact: ContactEntity)
        fun setUser(context : Context, userEntity: UserEntity)
        fun resetImage()
    }

}