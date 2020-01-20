package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.repository.editAccessPin.EditAccessPinRepository
import com.naposystems.pepito.ui.editAccessPin.IContractEditAccessPin
import com.naposystems.pepito.utility.SharedPreferencesManager
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