package com.naposystems.pepito.ui.contactProfile

import androidx.lifecycle.LiveData
import com.naposystems.pepito.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.pepito.dto.muteConversation.MuteConversationResDTO
import com.naposystems.pepito.entity.Contact
import retrofit2.Response

interface IContractContactProfile {

    interface ViewModel {
        fun getLocalContact(contactId : Int)
        fun updateNameFakeContact(contactId: Int, nameFake: String)
        fun updateNicknameFakeContact(contactId: Int, nameFake: String)
        fun updateAvatarFakeContact(contactId: Int, avatarFake: String)
        fun restoreContact(contactId: Int)
        fun updateContactSilenced(contactId : Int, contactSilenced : Boolean)
    }

    interface Repository {
        fun getLocalContact(contactId : Int): LiveData<Contact>
        suspend fun updateNameFakeContact(contactId: Int, nameFake: String)
        suspend fun updateNicknameFakeContact(contactId: Int, nicknameFake: String)
        suspend fun updateAvatarFakeContact(contactId: Int, avatarFake: String)
        suspend fun restoreContact(contactId: Int)
        suspend fun updateContactSilenced(contactId : Int, contactSilenced: Int)
        suspend fun saveTimeMuteConversation(contactId : Int, time: MuteConversationReqDTO) : Response<MuteConversationResDTO>
        fun getError(response: Response<MuteConversationResDTO>) : ArrayList<String>
    }

}