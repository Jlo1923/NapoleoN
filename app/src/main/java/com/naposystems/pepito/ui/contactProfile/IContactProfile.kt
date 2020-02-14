package com.naposystems.pepito.ui.contactProfile

import androidx.lifecycle.LiveData
import com.naposystems.pepito.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.pepito.dto.muteConversation.MuteConversationResDTO
import com.naposystems.pepito.entity.Contact
import retrofit2.Response

interface IContactProfile {

    interface ViewModel {
        fun getLocalContact(idContact : Int)
        fun updateNameFakeLocalContact(idContact: Int, nameFake: String)
        fun updateNicknameFakeLocalContact(idContact: Int, nameFake: String)
        fun updateAvatarFakeLocalContact(idContact: Int, avatarFake: String)
        fun restoreLocalContact(idContact: Int)
        fun deleteConversation(idContact: Int)
        fun updateContactSilenced(idContact : Int, contactSilenced : Boolean)
    }

    interface Repository {
        fun getLocalContact(idContact : Int): LiveData<Contact>
        suspend fun updateNameFakeLocalContact(idContact: Int, nameFake: String)
        suspend fun updateNicknameFakeLocalContact(idContact: Int, nicknameFake: String)
        suspend fun updateAvatarFakeLocalContact(idContact: Int, avatarFake: String)
        suspend fun restoreLocalContact(idContact: Int)
        suspend fun deleteConversation(idContact: Int)
        suspend fun updateContactSilenced(idContact : Int, contactSilenced: Int)
        suspend fun saveTimeMuteConversation(idContact : Int, time: MuteConversationReqDTO) : Response<MuteConversationResDTO>
        fun getError(response: Response<MuteConversationResDTO>) : ArrayList<String>
    }

}