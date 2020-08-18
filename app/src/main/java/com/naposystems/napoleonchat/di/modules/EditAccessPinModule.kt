package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.repository.editAccessPin.EditAccessPinRepository
import com.naposystems.napoleonchat.ui.editAccessPin.IContractEditAccessPin
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class EditAccessPinModule {

    @Provides
    @Singleton
    fun provideRepository(
        userLocalDataSource: UserLocalDataSource,
        sharedPreferencesManager: SharedPreferencesManager
    ): IContractEditAccessPin.Repository {
        return EditAccessPinRepository(userLocalDataSource, sharedPreferencesManager)
    }
}