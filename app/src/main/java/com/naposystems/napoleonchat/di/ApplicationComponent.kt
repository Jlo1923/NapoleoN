package com.naposystems.napoleonchat.di

import android.app.Application
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.di.module.general.*
import com.naposystems.napoleonchat.di.module.missnamed.CreateAccountModule
import com.naposystems.napoleonchat.di.module.sections.RepositoryModule
import com.naposystems.napoleonchat.di.module.sections.ViewModelModule
import com.naposystems.napoleonchat.di.module.share.*
import com.naposystems.napoleonchat.di.module.sources.local.DaoModule
import com.naposystems.napoleonchat.di.module.sources.local.LocalDataSourceModule
import com.naposystems.napoleonchat.di.module.sources.local.RoomModule
import com.naposystems.napoleonchat.di.module.sources.remote.RetrofitModule
import com.naposystems.napoleonchat.di.module.ui.ActivityModule
import com.naposystems.napoleonchat.di.module.ui.FragmentModule
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
        NapoleonApplicationModule::class,
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
        CallComponentsModule::class,
//        MoshiModule::class,
        //Sources Remote
        RetrofitModule::class,

        //Sources Local
        RoomModule::class,
        DaoModule::class,
        LocalDataSourceModule::class,

        //UI
        ActivityModule::class,
        FragmentModule::class,

        //Sections
        ViewModelModule::class,
        RepositoryModule::class,

        //Shared
        ContactProfileShareModule::class,
        ContactRepositoryShareModule::class,
        FriendShipActionShareModule::class,
        ShareContactModule::class,
        UserProfileShareModule::class,

        //missnamed
        CreateAccountModule::class]
)
interface ApplicationComponent : AndroidInjector<NapoleonApplication> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun create(application: Application): Builder

        fun build(): ApplicationComponent
    }

}