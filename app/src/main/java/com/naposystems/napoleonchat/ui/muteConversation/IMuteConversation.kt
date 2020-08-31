package com.naposystems.napoleonchat.ui.muteConversation

import com.naposystems.napoleonchat.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.napoleonchat.dto.muteConversation.MuteConversationResDTO
import retrofit2.Response

interface IMuteConversation {

    interface ViewModel {
        fun updateContactSilenced(idContact : Int, contactSilenced : Boolean)
    }

    interface Repository {
        suspend fun saveTimeMuteConversation(idContact : Int, time: MuteConversationReqDTO) : Response<MuteConversationResDTO>
        fun getError(response: Response<MuteConversationResDTO>) : String
        fun updateContactSilenced(idContact : Int, contactSilenced: Int)
    }
}