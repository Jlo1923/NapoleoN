package com.naposystems.pepito.ui.logout

import com.naposystems.pepito.dto.user.LogoutResDTO
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