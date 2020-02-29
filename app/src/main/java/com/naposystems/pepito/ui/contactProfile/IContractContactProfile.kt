package com.naposystems.pepito.ui.contactProfile

import androidx.lifecycle.LiveData
import com.naposystems.pepito.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.pepito.dto.muteConversation.MuteConversationResDTO
import com.naposystems.pepito.entity.Contact
import retrofit2.Response

interface IContractContactProfile {

    interface ViewModel {
        fun getLocalContact(idContact : Int)
        fun updateNameFakeContact(idContact: Int, nameFake: String)
        fun updateNicknameFakeContact(idContact: Int, nameFake: String)
        fun updateAvatarFakeContact(idContact: Int, avatarFake: String)
        fun restoreContact(idContact: Int)
        fun updateContactSilenced(idContact : Int, contactSilenced : Boolean)
    }

    interface Repository {
        fun getLocalContact(idContact : Int): LiveData<Contact>
        suspend fun updateNameFakeContact(idContact: Int, nameFake: String)
        suspend fun updateNicknameFakeContact(idContact: Int, nicknameFake: String)
        suspend fun updateAvatarFakeContact(idContact: Int, avatarFake: String)
        suspend fun restoreContact(idContact: Int)
        suspend fun updateContactSilenced(idContact : Int, contactSilenced: Int)
        suspend fun saveTimeMuteConversation(idContact : Int, time: MuteConversationReqDTO) : Response<MuteConversationResDTO>
        fun getError(response: Response<MuteConversationResDTO>) : ArrayList<String>
    }

}