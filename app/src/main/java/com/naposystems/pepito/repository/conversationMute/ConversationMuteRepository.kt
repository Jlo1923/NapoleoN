package com.naposystems.pepito.repository.conversationMute

import com.naposystems.pepito.db.dao.contact.ContactDao
import com.naposystems.pepito.dto.muteConversation.MuteConversationErrorDTO
import com.naposystems.pepito.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.pepito.dto.muteConversation.MuteConversationResDTO
import com.naposystems.pepito.ui.muteConversation.IMuteConversation
import com.naposystems.pepito.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class ConversationMuteRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val contactDao: ContactDao
) : IMuteConversation.Repository {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    override suspend fun saveTimeMuteConversation(idContact: Int, time: MuteConversationReqDTO) : Response<MuteConversationResDTO> {
        return napoleonApi.updateMuteConversation(idContact, time)
    }

    override fun updateContactSilenced(idContact: Int, contactSilenced : Int) {
        contactDao.updateContactSilenced(idContact, contactSilenced)
    }

    override fun getError(response: Response<MuteConversationResDTO>): String {
        val adapter = moshi.adapter(MuteConversationErrorDTO::class.java)
        val muteConversationInfoError = adapter.fromJson(response.errorBody()!!.string())
        return muteConversationInfoError!!.error
    }

}