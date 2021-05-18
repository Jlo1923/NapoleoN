package com.naposystems.napoleonchat.ui.previewImage

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import javax.inject.Inject

class PreviewImageViewModel @Inject constructor() : ViewModel(), IContractPreviewImage.ViewModel {

    private val _image = MutableLiveData<Any>()
    val image: LiveData<Any>
        get() = _image

    init {
        _image.value = null
    }

    override fun setContact(context: Context, contact: ContactEntity) {
        if (contact.imageUrlFake.isEmpty()) {
            _image.value = null
        } else {
            _image.value = contact.imageUrlFake
        }
    }

    override fun setUser(context: Context, userEntity: UserEntity) {
        when {
            userEntity.imageUrl.isNotEmpty() -> {
                _image.value = userEntity.imageUrl
            }
            else -> {
                _image.value = null
            }
        }
    }

    override fun resetImage() {
        _image.value = null
    }

}