package com.naposystems.napoleonchat.ui.logout

import com.naposystems.napoleonchat.source.remote.dto.user.LogoutResDTO
import retrofit2.Response

interface IContractLogout {

    interface ViewModel {
        fun logOut()
    }

    interface Repository {
        suspend fun logOut(): Response<LogoutResDTO>
        suspend fun clearData()
    }
}