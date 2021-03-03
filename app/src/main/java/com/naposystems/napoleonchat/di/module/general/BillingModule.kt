package com.naposystems.napoleonchat.di.module.general

import android.content.Context
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.subscription.BillingClientLifecycle
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class BillingModule {

    @Provides
    @Singleton
    fun provideBillingClient(context: Context): BillingClientLifecycle {
        return BillingClientLifecycle(context as NapoleonApplication)
    }

}