package com.naposystems.napoleonchat.dialog.logout

import com.naposystems.napoleonchat.source.remote.dto.user.LogoutResDTO
import retrofit2.Response

interface LogoutDialogRepository {
    suspend fun logOut(): Response<LogoutResDTO>
    suspend fun clearData()
}