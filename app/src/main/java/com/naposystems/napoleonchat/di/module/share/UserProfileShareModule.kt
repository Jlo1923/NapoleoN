package com.naposystems.napoleonchat.di.module.share

import com.naposystems.napoleonchat.repository.sharedRepository.UserProfileShareRepository
import com.naposystems.napoleonchat.utility.sharedViewModels.userProfile.IContractUserProfileShare
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
abstract class UserProfileShareModule {
    @Binds
    @Singleton
    abstract fun bindUserProfileShareRepository(
        repository: UserProfileShareRepository
    ): IContractUserProfileShare.Repository
}