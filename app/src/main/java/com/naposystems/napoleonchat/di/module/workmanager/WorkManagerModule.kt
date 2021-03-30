package com.naposystems.napoleonchat.di.module.workmanager

import android.content.Context
import androidx.work.WorkManager
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
class WorkManagerModule {

    @Provides
    fun bindWorkManager(
        context: Context
    ) = WorkManager.getInstance(context)

}