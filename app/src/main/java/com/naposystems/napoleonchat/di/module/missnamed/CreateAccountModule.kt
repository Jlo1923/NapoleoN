package com.naposystems.napoleonchat.di.module.missnamed

import com.naposystems.napoleonchat.repository.accessPin.CreateAccountRepository
import com.naposystems.napoleonchat.ui.register.accessPin.IContractAccessPin
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class CreateAccountModule {

    @Binds
    @Singleton
    abstract fun bindCreateAccountRepository(
        repository: CreateAccountRepository
    ): IContractAccessPin.Repository
}