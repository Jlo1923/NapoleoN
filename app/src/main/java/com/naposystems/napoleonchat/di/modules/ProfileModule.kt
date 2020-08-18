package com.naposystems.napoleonchat.di.modules

import android.content.Context
import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.repository.profile.ProfileRepository
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ProfileModule {

    @Provides
    @Singleton
    fun provideRepository(
        context: Context,
        userLocalDataSource: UserLocalDataSource,
        sharedPreferencesManager: SharedPreferencesManager
    ): ProfileRepository {
        return ProfileRepository(context, userLocalDataSource, sharedPreferencesManager)
    }
}