package com.example.bikefm2.Network

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.example.bikefm2.BikeFmApp
import com.example.bikefm2.R
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class NetworkService {
    fun getApolloClient(): ApolloClient {
        val okHttp = OkHttpClient
            .Builder()
            .build()

        return ApolloClient.builder()
            .serverUrl(BikeFmApp.applicationContext().getString(R.string.server_url))
            .okHttpClient(okHttp)
            .build()
    }

    fun getApolloClientWithTokenIntercetor(token: String): ApolloClient {

        try {
            val httpClient = OkHttpClient.Builder()
                .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                    val original = chain.request()

                    val newRequest = original.newBuilder()
                        .addHeader("token", token)
                        .build();
                    return@Interceptor chain.proceed(newRequest)
                })
                .build()
            return ApolloClient.builder()
                .serverUrl(BikeFmApp.applicationContext().getString(R.string.server_url))
                .okHttpClient(httpClient)
                .build()
        }
        catch (exception: IOException){
            Log.e("apolllo", exception.message)
            return getApolloClient()
        }
    }

    companion object {
        private var mInstance = NetworkService()

        fun getInstance(): NetworkService {
            return mInstance
        }
    }
}
