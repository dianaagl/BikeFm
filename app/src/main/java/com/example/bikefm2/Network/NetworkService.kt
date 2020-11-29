package com.example.bikefm2.Network

import com.apollographql.apollo.ApolloClient
import com.example.bikefm2.BikeFmApp
import com.example.bikefm2.R
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request

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

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()

                val builder: Request.Builder = original
                    .newBuilder()
                    .method(original.method, original.body)

                builder.header("Authorization", "Bearer $token")
                return@Interceptor chain.proceed(builder.build())
            })
            .build()

        return ApolloClient.builder()
            .serverUrl(BikeFmApp.applicationContext().getString(R.string.server_url))
            .okHttpClient(httpClient)
            .build()
    }

    companion object {
        private var mInstance: NetworkService? = null

        fun getInstance(): NetworkService? {
            if (mInstance == null) {
                mInstance = NetworkService()
            }
            return mInstance
        }
    }
}
