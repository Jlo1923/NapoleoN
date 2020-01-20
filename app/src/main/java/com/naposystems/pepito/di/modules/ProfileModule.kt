package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.repository.profile.ProfileRepository
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ProfileModule {

    @Provides
    @Singleton
    fun provideRepository(
        userLocalDataSource: UserLocalDataSource,
        napoleonApi: NapoleonApi
    ): ProfileRepository {
        return ProfileRepository(userLocalDataSource, napoleonApi)
    }
}