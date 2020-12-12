package com.example.bikefm2.Network

import LoginQuery
import RegistrationMutation
import SetLocationMutation
import VerifyQuery
import android.location.Location
import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.example.bikefm2.data.model.Friend
import com.example.bikefm2.data.model.LoggedInUser
import com.example.bikefm2.ui.login.LoginResult
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class NetworkApiCall @Inject constructor() {

    suspend fun loginUser(username: String, password: String): LoggedInUser{
        return suspendCoroutine { continuation ->
            val client = NetworkService.getInstance()
                ?.getApolloClient()
            client
                ?.query(LoginQuery(username, password))
                ?.enqueue(object : ApolloCall.Callback<LoginQuery.Data>() {

                    override fun onResponse(response: Response<LoginQuery.Data>) {
                        if (response.data?.login()?.error() == null) {
                            continuation.resume(LoggedInUser(
                                displayName = response.data?.login()?.user()
                                    ?.name()
                                    .toString(),
                                userId = response.data?.login()?.user()?._id().toString(),
                                token = response.data?.login()?.token().toString(),
                                friendsList = listOf(
                                    Friend("1", "Brien", "philipps"),
                                    Friend("2", "Andrew", "Pancake")
                                )
                            ))
                        }
                        else{
                            continuation.resumeWithException(Exception(response.data?.login()?.error().toString()))
                        }
                    }

                    override fun onFailure(e: ApolloException) {
                        continuation.resumeWithException(e)
                    }
                })
        }

    }
    suspend fun registerUser(username: String, password: String): LoggedInUser{
        return suspendCoroutine { continuation ->
            val client = NetworkService.getInstance()
                ?.getApolloClient()
            client
                ?.mutate(RegistrationMutation(username, password))
                ?.enqueue(object : ApolloCall.Callback<RegistrationMutation.Data>() {
                    override fun onResponse(response: Response<RegistrationMutation.Data>) {
                        if (response.data?.registration()?.error() == null) {
                            val user = LoggedInUser(
                                displayName = response.data?.registration()?.user()
                                    ?.name()
                                    .toString(),
                                userId = response.data?.registration()?.user()?._id().toString(),
                                token = response.data?.registration()?.token().toString(),
                                friendsList = listOf(
                                    Friend("1", "Brien", "philipps"),
                                    Friend("2", "Andrew", "Pancake"))
                            )
                            continuation.resume(user)
                        }
                        else{
                            continuation.resumeWithException(Exception(response.data?.registration()?.error().toString()))
                        }
                    }

                    override fun onFailure(e: ApolloException) {
                        continuation.resumeWithException(e)
                    }
                })
        }
    }
    suspend fun setUserLocation(token: String, loc: Location): Boolean{
        return suspendCoroutine { continuation ->
            val client = NetworkService.getInstance()
                ?.getApolloClientWithTokenIntercetor(token!!)

            client
                ?.mutate(SetLocationMutation(loc.latitude, loc.longitude))
                ?.enqueue(object : ApolloCall.Callback<SetLocationMutation.Data>() {
                    override fun onResponse(response: Response<SetLocationMutation.Data>) {
                        continuation.resume(response.data?.setLocation()!!)
                    }

                    override fun onFailure(e: ApolloException) {
                        continuation.resumeWithException(e)
                    }
                })
        }
    }
    suspend fun verifyUser(token: String): LoggedInUser?{
        return suspendCoroutine { continuation ->
            val client = NetworkService.getInstance()
                ?.getApolloClient()

            client
                ?.query(VerifyQuery(token))
                ?.enqueue(object : ApolloCall.Callback<VerifyQuery.Data>() {

                    override fun onResponse(response: Response<VerifyQuery.Data>) {
                        var user = response.data?.user()
                        if (response.hasErrors() || user == null) {
                            continuation.resume(null)
                        }
                        else{
                            val user = LoggedInUser(
                                displayName = response.data?.user()
                                    ?.name()!!,
                                userId = response.data?.user()?._id()!!,
                                token = token,
                                friendsList = listOf(
                                    Friend("1", "Brien", "philipps"),
                                    Friend("2", "Andrew", "Pancake"))
                            )
                            continuation.resume(user)

                        }
                    }

                    override fun onFailure(e: ApolloException) {
                        continuation.resumeWithException(e)
                    }
                })
        }
    }

}