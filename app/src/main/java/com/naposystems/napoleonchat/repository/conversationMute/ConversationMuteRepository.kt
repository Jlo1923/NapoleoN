package com.naposystems.napoleonchat.repository.conversationMute

import com.naposystems.napoleonchat.source.local.dao.ContactDao
import com.naposystems.napoleonchat.source.remote.dto.muteConversation.MuteConversationErrorDTO
import com.naposystems.napoleonchat.source.remote.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.napoleonchat.source.remote.dto.muteConversation.MuteConversationResDTO
import com.naposystems.napoleonchat.ui.muteConversation.IMuteConversation
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
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