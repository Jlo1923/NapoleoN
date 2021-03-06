package com.naposystems.napoleonchat.di.module.share

import com.naposystems.napoleonchat.repository.sharedRepository.ShareContactRepository
import com.naposystems.napoleonchat.utility.sharedViewModels.contact.IContractShareContact
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
abstract class ShareContactModule {
    @Binds
    @Singleton
    abstract fun bindShareContactRepository(
        repository: ShareContactRepository
    ): IContractShareContact.Repository
}