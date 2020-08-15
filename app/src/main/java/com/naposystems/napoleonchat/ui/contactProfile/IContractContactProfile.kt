package com.naposystems.napoleonchat.ui.contactProfile

import com.naposystems.napoleonchat.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.napoleonchat.dto.muteConversation.MuteConversationResDTO
import retrofit2.Response

interface IContractContactProfile {

    interface ViewModel {
        fun updateAvatarFakeContact(contactId: Int, avatarFake: String)
        fun restoreContact(contactId: Int)
        fun updateContactSilenced(contactId : Int, contactSilenced : Boolean)
        fun restoreImageByContact(contactId : Int)
    }

    interface Repository {
        suspend fun updateAvatarFakeContact(contactId: Int, avatarFake: String)
        suspend fun restoreContact(contactId: Int)
        suspend fun updateContactSilenced(contactId : Int, contactSilenced: Int)
        suspend fun saveTimeMuteConversation(contactId : Int, time: MuteConversationReqDTO) : Response<MuteConversationResDTO>
        fun getError(response: Response<MuteConversationResDTO>) : ArrayList<String>
        suspend fun restoreImageByContact(contactId : Int)
    }
}