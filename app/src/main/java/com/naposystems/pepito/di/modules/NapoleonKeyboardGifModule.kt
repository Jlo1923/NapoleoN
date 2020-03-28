package com.naposystems.pepito.di.modules

import android.content.Context
import com.naposystems.pepito.repository.napoleonKeyboardGif.NapoleonKeyboardGifRepository
import com.naposystems.pepito.ui.napoleonKeyboardGif.IContractNapoleonKeyboardGif
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class NapoleonKeyboardGifModule {

    @Provides
    @Singleton
    fun provideRepository(
        context: Context,
        napoleonApi: NapoleonApi
    ): IContractNapoleonKeyboardGif.Repository {
        return NapoleonKeyboardGifRepository(context, napoleonApi)
    }
}