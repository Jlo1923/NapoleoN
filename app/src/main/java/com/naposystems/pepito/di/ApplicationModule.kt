package com.naposystems.pepito.di

import android.app.Application
import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.naposystems.pepito.repository.socket.SocketRepository
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.LocaleHelper
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import com.naposystems.pepito.webService.socket.SocketService
import com.naposystems.pepito.webService.socket.IContractSocketService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class ApplicationModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application
    }

    @Provides
    @Singleton
    fun provideSharedPreferencesManager(context: Context): SharedPreferencesManager {
        return SharedPreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideHttpClient(
        context: Context,
        sharedPreferencesManager: SharedPreferencesManager
    ): OkHttpClient {
        val httpClient = OkHttpClient.Builder()

        httpClient.addNetworkInterceptor(StethoInterceptor())

        httpClient.addInterceptor { chain ->
            val original = chain.request()
            val firebaseInstanceId = sharedPreferencesManager.getString(
                Constants.SharedPreferences.PREF_FIREBASE_ID,
                ""
            )

            val socketId = sharedPreferencesManager.getString(
                Constants.SharedPreferences.PREF_SOCKET_ID,
                ""
            )

            val request: Request = original.newBuilder()
                .header("languageIso", LocaleHelper.getLanguagePreference(context))
                .header("X-API-Key", firebaseInstanceId)
                .header("X-Socket-ID", socketId)
                .method(original.method(), original.body())
                .build()

            chain.proceed(request)
        }

        return httpClient.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(httpClient: OkHttpClient): NapoleonApi {

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.NapoleonApi.BASE_URL)
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(NapoleonApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSocketClient(
        sharedPreferencesManager: SharedPreferencesManager,
        socketRepository: IContractSocketService.Repository
    ): IContractSocketService.SocketService {
        return SocketService(
            sharedPreferencesManager,
            socketRepository
        )
    }
}