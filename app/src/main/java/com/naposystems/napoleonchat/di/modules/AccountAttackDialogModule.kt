package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.accountAttackDialog.AccountAttackDialogRepository
import com.naposystems.napoleonchat.ui.accountAttack.IContractAccountAttackDialog
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AccountAttackDialogModule {

    @Provides
    @Singleton
    fun provideRepository(
        sharedPreferencesManager: SharedPreferencesManager,
        napoleonApi: NapoleonApi
    ): IContractAccountAttackDialog.Repository {
        return AccountAttackDialogRepository(sharedPreferencesManager, napoleonApi)
    }
}