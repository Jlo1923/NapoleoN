package com.naposystems.pepito.webService

import android.content.Context
import com.naposystems.pepito.reactive.RxBus
import com.naposystems.pepito.reactive.RxEvent
import com.naposystems.pepito.utility.Constants
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
        try {
            val comand = Runtime.getRuntime().exec(Constants.ValidConnection.REQUEST_PIN)
            val valid = comand.waitFor()
            return valid == 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}