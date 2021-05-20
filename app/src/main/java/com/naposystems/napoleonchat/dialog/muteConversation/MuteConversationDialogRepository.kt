package com.naposystems.napoleonchat.dialog.muteConversation

import com.naposystems.napoleonchat.source.remote.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.napoleonchat.source.remote.dto.muteConversation.MuteConversationResDTO
import retrofit2.Response

interface MuteConversationDialogRepository {
    suspend fun saveTimeMuteConversation(
        idContact: Int,
        time: MuteConversationReqDTO
    ): Response<MuteConversationResDTO>

    fun getError(response: Response<MuteConversationResDTO>): String
    fun updateContactSilenced(idContact: Int, contactSilenced: Int)
}