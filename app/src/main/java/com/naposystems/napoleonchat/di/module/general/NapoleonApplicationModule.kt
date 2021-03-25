package com.naposystems.napoleonchat.di.module.general

import android.content.Context
import com.naposystems.napoleonchat.app.NapoleonApplication
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class NapoleonApplicationModule {

    @Provides
    @Singleton
    fun provideNapoleonApplication(context: Context): NapoleonApplication {
        return context as NapoleonApplication
    }

}