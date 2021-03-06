package com.naposystems.napoleonchat.webService

import android.content.Context
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.utility.Constants
import okhttp3.Interceptor
import okhttp3.Response

class NetworkConnectionInterceptor constructor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isOnlineNet()) {
            RxBus.publish(RxEvent.NoInternetConnection())
            throw NoConnectivityException()
        }

        val builder = chain.request().newBuilder()
        return chain.proceed(builder.build())
    }

    private fun isOnlineNet(): Boolean {
        return try {
            val comand = Runtime.getRuntime().exec(Constants.ValidConnection.REQUEST_PIN)
            val valid = comand.waitFor()
            valid == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}