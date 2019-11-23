package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.repository.mainActivity.MainActivityRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class MainActivityModule {

    @Provides
    @Singleton
    fun provideRepository(userLocalDataSource: UserLocalDataSource): MainActivityRepository {
        return MainActivityRepository(userLocalDataSource)
    }
}