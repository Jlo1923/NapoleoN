package com.naposystems.napoleonchat.di.module.share

import com.naposystems.napoleonchat.repository.sharedRepository.ContactProfileShareRepository
import com.naposystems.napoleonchat.utility.sharedViewModels.contactProfile.IContractContactProfileShare
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class ContactProfileShareModule {
    @Binds
    @Singleton
    abstract fun bindContactProfileShareRepository(
        repository: ContactProfileShareRepository
    ): IContractContactProfileShare.Repository
}