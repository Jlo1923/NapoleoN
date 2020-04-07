package com.naposystems.pepito.ui.previewImage

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import javax.inject.Inject

class PreviewImageViewModel@Inject constructor() : ViewModel(), IContractPreviewImage.ViewModel {

    private val _image = MutableLiveData<Any>()
    val image : LiveData<Any>
    get() = _image

    init {
        _image.value = null
    }

    override fun setContact(context : Context,contact: Contact) {
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

    override fun setUser(context: Context, user: User) {
        when {
            user.imageUrl.isNotEmpty() -> {
                _image.value = user.imageUrl
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