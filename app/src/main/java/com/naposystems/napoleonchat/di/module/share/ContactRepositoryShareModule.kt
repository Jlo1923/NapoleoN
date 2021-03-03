package com.naposystems.napoleonchat.di.module.share

import com.naposystems.napoleonchat.repository.sharedRepository.ContactRepositoryShareRepository
import com.naposystems.napoleonchat.utility.sharedViewModels.contactRepository.IContractContactRepositoryShare
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class ContactRepositoryShareModule {
    @Binds
    @Singleton
    abstract fun bindContactRepositoryShareRepository(
        repository: ContactRepositoryShareRepository
    ): IContractContactRepositoryShare.Repository
}