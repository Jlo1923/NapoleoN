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

class PreviewImageViewModel@Inject constructor() : ViewModel(), IContractPreviewImage.ViewModel {

    private val _image = MutableLiveData<Any>()
    val image : LiveData<Any>
    get() = _image

    init {
        _image.value = null
    }

    override fun setContact(context : Context,contact: ContactEntity) {
        when {
            contact.imageUrlFake.isNotEmpty() -> {
                val imageUri = Utils.getFileUri(
                    context = context,
                    fileName = contact.imageUrlFake,
                    subFolder = Constants.NapoleonCacheDirectories.IMAGE_FAKE_CONTACT.folder
                )
                _image.value = imageUri
            }
            contact.imageUrl.isNotEmpty() -> {
                _image.value = contact.imageUrl
            }
            else -> {
                _image.value = null
            }
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