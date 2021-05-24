package com.naposystems.napoleonchat.dialog.muteConversation

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.source.remote.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import kotlinx.coroutines.launch
import javax.inject.Inject

class MuteConversationDialogViewModel
@Inject constructor(
    private val context: Context,
    private val repository: MuteConversationDialogRepository
) : ViewModel() {

    var timeMuteConversation: Int = 0

    private val _mutedConversation = MutableLiveData<Boolean>()
    val mutedConversation: LiveData<Boolean>
        get() = _mutedConversation

    private val _muteConversationWsError = MutableLiveData<String>()
    val muteConversationWsError: LiveData<String>
        get() = _muteConversationWsError

    fun updateContactSilenced(idContact: Int, contactSilenced: Boolean) {
        viewModelScope.launch {
            try {
                val response = repository.saveTimeMuteConversation(
                    idContact,
                    buildObjectMuteConversation(timeMuteConversation)
                )

                if (response.isSuccessful) {
                    repository.updateContactSilenced(
                        idContact,
                        Utils.convertBooleanToInvertedInt(contactSilenced)
                    )
                    _mutedConversation.value = true
                } else {
                    _muteConversationWsError.value = repository.getError(response)
                }
            } catch (ex: Exception) {
                _muteConversationWsError.value = context.getString(R.string.text_fail)
            }
        }
    }

    private fun buildObjectMuteConversation(time: Int): MuteConversationReqDTO {
        return when (time) {
            Constants.TimeMuteConversation.ONE_HOUR.time -> MuteConversationReqDTO(time, 1)
            Constants.TimeMuteConversation.EIGHT_HOURS.time -> MuteConversationReqDTO(time, 1)
            Constants.TimeMuteConversation.ONE_DAY.time -> MuteConversationReqDTO(time, 2)
            Constants.TimeMuteConversation.ONE_YEAR.time -> MuteConversationReqDTO(time, 2)
            else -> MuteConversationReqDTO()
        }
    }
}
