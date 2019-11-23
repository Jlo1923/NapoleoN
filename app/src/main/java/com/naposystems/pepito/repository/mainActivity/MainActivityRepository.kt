package com.naposystems.pepito.repository.mainActivity

import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.ui.mainActivity.IContractMainActivity
import javax.inject.Inject

class MainActivityRepository @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource
) :
    IContractMainActivity.Repository {

    override suspend fun getUser(firebaseId: String): User {
        return  userLocalDataSource.getUser(firebaseId)
    }
}