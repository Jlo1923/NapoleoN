package com.naposystems.napoleonchat.di

import android.app.Application
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.di.module.general.*
import com.naposystems.napoleonchat.di.module.mediastore.MediaStoreModule
import com.naposystems.napoleonchat.di.module.repository.DialogRepositoryModule
import com.naposystems.napoleonchat.di.module.repository.RepositoryModule
import com.naposystems.napoleonchat.di.module.repository.SharedRepositoryModule
import com.naposystems.napoleonchat.di.module.sources.local.DaoModule
import com.naposystems.napoleonchat.di.module.sources.local.LocalDataSourceModule
import com.naposystems.napoleonchat.di.module.sources.local.RoomModule
import com.naposystems.napoleonchat.di.module.sources.remote.RetrofitModule
import com.naposystems.napoleonchat.di.module.ui.ActivityModule
import com.naposystems.napoleonchat.di.module.ui.FragmentModule
import com.naposystems.napoleonchat.di.module.viewModel.DialogViewModelModule
import com.naposystems.napoleonchat.di.module.viewModel.SharedViewModelModule
import com.naposystems.napoleonchat.di.module.viewModel.ViewModelModule
import com.naposystems.napoleonchat.di.module.workmanager.WorkManagerModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        //General
        AndroidSupportInjectionModule::class,
        BillingModule::class,
        ContextModule::class,
        ServiceModule::class,
        SharedPreferencesModule::class,
        PusherModule::class,
        SocketModule::class,
        WebRTCClientModule::class,
        CryptoModule::class,
        SyncManagerModule::class,
        UtilsModule::class,
        NotificationServicemodule::class,

        //Sources Remote
        RetrofitModule::class,

        //Sources Local
        RoomModule::class,
        DaoModule::class,
        LocalDataSourceModule::class,

        //UI
        ActivityModule::class,
        FragmentModule::class,

        //ViewModel
        ViewModelModule::class,
        DialogViewModelModule::class,
        SharedViewModelModule::class,

        //Repository
        RepositoryModule::class,
        DialogRepositoryModule::class,
        SharedRepositoryModule::class,

        // MediaStore
        MediaStoreModule::class,

        // WorkManager
        WorkManagerModule::class,

    ]
)
interface ApplicationComponent : AndroidInjector<NapoleonApplication> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun create(application: Application): Builder

        fun build(): ApplicationComponent

    }

}