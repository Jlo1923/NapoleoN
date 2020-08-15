package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.repository.previewBackgrounChat.PreviewBackgroundChatRepository
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PreviewBackgroundChatModule {

    @Provides
    @Singleton
    fun provideRepository(sharedPreferencesManager: SharedPreferencesManager, userLocalDataSource: UserLocalDataSource): PreviewBackgroundChatRepository {
        return PreviewBackgroundChatRepository(sharedPreferencesManager, userLocalDataSource)
    }
}