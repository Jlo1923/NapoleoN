package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.repository.sharedRepository.UserProfileShareRepository
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.sharedViewModels.userProfile.IContractUserProfileShare
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides

@Module
class UserProfileShareModule {
    @Provides
    fun provideRepository(
        napoleonApi: NapoleonApi,
        userLocalDataSource: UserLocalDataSource,
        sharedPreferencesManager: SharedPreferencesManager
    ): IContractUserProfileShare.Repository {
        return UserProfileShareRepository(
            napoleonApi,
            userLocalDataSource,
            sharedPreferencesManager
        )
    }
}