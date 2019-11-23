package com.naposystems.pepito.ui.mainActivity

import com.naposystems.pepito.entity.User

interface IContractMainActivity {

    interface ViewModel {
        fun getUser(firebaseId: String)
    }

    interface Repository {
        suspend fun getUser(firebaseId: String) : User
    }
}