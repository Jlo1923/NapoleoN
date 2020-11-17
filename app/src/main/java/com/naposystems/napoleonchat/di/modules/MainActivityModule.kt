package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.repository.mainActivity.MainActivityRepository
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class MainActivityModule {

    @Provides
    @Singleton
    fun provideRepository(
        contactLocalDataSource: ContactDataSource,
        userLocalDataSource: UserLocalDataSource,
        sharedPreferencesManager: SharedPreferencesManager
    ): MainActivityRepository {
        return MainActivityRepository(
            contactLocalDataSource,
            userLocalDataSource,
            sharedPreferencesManager
        )
    }
}