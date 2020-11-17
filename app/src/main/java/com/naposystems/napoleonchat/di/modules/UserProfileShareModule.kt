package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.repository.sharedRepository.UserProfileShareRepository
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.sharedViewModels.userProfile.IContractUserProfileShare
import com.naposystems.napoleonchat.webService.NapoleonApi
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