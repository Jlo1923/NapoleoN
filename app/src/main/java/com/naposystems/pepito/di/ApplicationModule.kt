package com.naposystems.pepito.di

import android.app.Application
import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.naposystems.pepito.BuildConfig
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Constants.NapoleonApi.CREATE_ACCOUNT
import com.naposystems.pepito.utility.Constants.NapoleonApi.GENERATE_CODE
import com.naposystems.pepito.utility.Constants.NapoleonApi.GET_QUESTIONS_OLD_USER
import com.naposystems.pepito.utility.Constants.NapoleonApi.GET_RECOVERY_QUESTIONS
import com.naposystems.pepito.utility.Constants.NapoleonApi.SEND_ANSWERS
import com.naposystems.pepito.utility.Constants.NapoleonApi.SEND_MESSAGE_ATTACHMENT
import com.naposystems.pepito.utility.Constants.NapoleonApi.VALIDATE_NICKNAME
import com.naposystems.pepito.utility.Constants.NapoleonApi.VERIFICATE_CODE
import com.naposystems.pepito.crypto.Crypto
import com.naposystems.pepito.utility.Constants.NapoleonApi.VALIDATE_ANSWERS_OLD_USER
import com.naposystems.pepito.utility.Constants.NapoleonApi.VALIDATE_PASSWORD_OLD_ACCOUNT
import com.naposystems.pepito.utility.LocaleHelper
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import com.naposystems.pepito.webService.socket.IContractSocketService
import com.naposystems.pepito.webService.socket.SocketService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.*
import okio.Buffer
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

val NO_ENCRYPT_REQUESTS: Array<String> = arrayOf(
    GENERATE_CODE,
    VERIFICATE_CODE,
    VALIDATE_NICKNAME,
    CREATE_ACCOUNT,
    GET_RECOVERY_QUESTIONS,
    SEND_ANSWERS,
    SEND_MESSAGE_ATTACHMENT,
    GET_QUESTIONS_OLD_USER,
    VALIDATE_PASSWORD_OLD_ACCOUNT,
    VALIDATE_ANSWERS_OLD_USER
)

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
            .readTimeout(15, TimeUnit.MINUTES)
            .connectTimeout(15, TimeUnit.MINUTES)

        httpClient.addNetworkInterceptor(StethoInterceptor())

//        httpClient.addInterceptor(NetworkConnectionInterceptor(context))
        try {
            httpClient.addInterceptor { chain ->

                val firebaseInstanceId = sharedPreferencesManager.getString(
                    Constants.SharedPreferences.PREF_FIREBASE_ID,
                    ""
                )

                val socketId = sharedPreferencesManager.getString(
                    Constants.SharedPreferences.PREF_SOCKET_ID,
                    ""
                )

                val secretKey = sharedPreferencesManager.getString(
                    Constants.SharedPreferences.PREF_SECRET_KEY,
                    ""
                )

                val original = chain.request()

                if (BuildConfig.ENCRYPT_API) {
                    encryptRequest(
                        original,
                        context,
                        firebaseInstanceId,
                        socketId,
                        secretKey,
                        chain
                    )
                } else {
                    val request: Request = original.newBuilder()
                        .header("languageIso", LocaleHelper.getLanguagePreference(context))
                        .header("X-API-Key", firebaseInstanceId)
                        .header("X-Socket-ID", socketId)
                        .method(original.method(), original.body())
                        .build()

                    chain.proceed(request)
                }

            }
        } catch (e: Exception) {
            Timber.e(e)
        }

//        httpClient.addInterceptor(GzipRequestInterceptor())

        return httpClient.build()
    }

    private fun encryptRequest(
        original: Request,
        context: Context,
        firebaseInstanceId: String,
        socketId: String,
        secretKey: String,
        chain: Interceptor.Chain
    ): Response {
        val body = original.body()

        val rawBodyRequest = bodyToString(body)

        val cripto = Crypto()

        val request: Request.Builder = original.newBuilder()
            .header("languageIso", LocaleHelper.getLanguagePreference(context))
            .header("X-API-Key", firebaseInstanceId)
            .header("X-Socket-ID", socketId)

        val isNotEncryptedRequest = NO_ENCRYPT_REQUESTS.find {
            val originalUrl = original.url()
            val algo = BuildConfig.BASE_URL + it

            if (algo == BuildConfig.BASE_URL + GET_RECOVERY_QUESTIONS || algo == BuildConfig.BASE_URL + GET_QUESTIONS_OLD_USER) {

                var pathSegments = ""

                originalUrl.pathSegments().forEachIndexed { index, s ->
                    if (index < originalUrl.pathSegments().size - 1) {
                        pathSegments += "$s/"
                    }
                }

                val rawUrl = "${originalUrl.scheme()}://${originalUrl.host()}/$pathSegments"
                val cleanUrl = ((BuildConfig.BASE_URL + it).replaceAfter("/{", "")).replace("{", "")

                pathSegments.isNotEmpty() && rawUrl == cleanUrl
            } else {
                BuildConfig.BASE_URL + it == original.url().uri().toString()
            }
        } != null

        if (original.method() == "GET" || isNotEncryptedRequest) {
            request.method(original.method(), original.body())
        } else {
            body?.let { requestBody ->
                if (requestBody.contentLength() > 0L) {

                    val jsonObject = JSONObject()
                    jsonObject.put(
                        Constants.DATA_CRYPT,
                        cripto.encryptPlainTextWithRandomIV(rawBodyRequest!!, secretKey)
                    )

                    val newRequestBody: RequestBody =
                        RequestBody.create(
                            MediaType.parse("application/json"),
                            jsonObject.toString()
                        )

                    request.method(original.method(), newRequestBody)

                } else {
                    request.method(original.method(), original.body())
                }
            }
        }

        Timber.d(
            "BuildConfig: ${BuildConfig.ENCRYPT_API} && isNotEncryptedRequest: $isNotEncryptedRequest"
        )

        return if (original.url().host() != BuildConfig.HOST_URL) {
            chain.proceed(request.build())
        } else if (BuildConfig.ENCRYPT_API && !isNotEncryptedRequest ||
            BuildConfig.BASE_URL + SEND_MESSAGE_ATTACHMENT == original.url().uri().toString()
        ) {
            try {
                decryptResponse(chain, request, cripto, secretKey)
            } catch (e: Exception) {
                Timber.e(e)
                chain.proceed(request.build())
            }
        } else {
            chain.proceed(request.build())
        }
    }

    private fun decryptResponse(
        chain: Interceptor.Chain,
        request: Request.Builder,
        crypto: Crypto,
        secretKey: String
    ): Response {
        val response: Response = chain.proceed(request.build())

        val rawResponse = response.body()!!.string()
        Timber.e("Respuesta de mierda: ${rawResponse.trimIndent()}")

        val jsonObject = JSONObject(rawResponse)

        val decryptedData = crypto.decryptCipherTextWithRandomIV(
            jsonObject.getString("datacrypt"),
            secretKey
        )

        val responseBody =
            ResponseBody.create(MediaType.parse("application/json"), decryptedData)

        return Response.Builder()
            .code(response.code())
            .message(response.message())
            .protocol(response.protocol())
            .request(chain.request())
            .body(responseBody)
            .build()
    }

    private fun bodyToString(request: RequestBody?): String? {
        return try {
            val buffer = Buffer()
            if (request != null) request.writeTo(buffer) else return ""
            buffer.readUtf8()
        } catch (e: IOException) {
            "did not work"
        }
    }

    @Provides
    @Singleton
    fun provideRetrofit(httpClient: OkHttpClient): NapoleonApi {

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(NapoleonApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSocket(): Socket = IO.socket(BuildConfig.SOCKET_BASE_URL)

    @Provides
    @Singleton
    fun provideSocketClient(
        context: Context,
        socket: Socket,
        sharedPreferencesManager: SharedPreferencesManager,
        socketRepository: IContractSocketService.Repository
    ): IContractSocketService.SocketService {
        return SocketService(
            context,
            socket,
            sharedPreferencesManager,
            socketRepository
        )
    }
}