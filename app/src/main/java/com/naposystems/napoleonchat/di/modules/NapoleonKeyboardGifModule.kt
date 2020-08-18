package com.naposystems.napoleonchat.di.modules

import android.content.Context
import com.naposystems.napoleonchat.repository.napoleonKeyboardGif.NapoleonKeyboardGifRepository
import com.naposystems.napoleonchat.ui.napoleonKeyboardGif.IContractNapoleonKeyboardGif
import com.naposystems.napoleonchat.webService.NapoleonApi
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