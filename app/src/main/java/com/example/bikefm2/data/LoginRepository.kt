package com.example.bikefm2.data

import LoginQuery
import RegistrationMutation
import androidx.lifecycle.MutableLiveData
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.example.bikefm2.Network.NetworkService
import com.example.bikefm2.data.model.LoggedInUser
import com.example.bikefm2.ui.login.LoginResult
import javax.inject.Inject


/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository @Inject constructor() {
    lateinit var cachedUser: MutableLiveData<LoggedInUser>
    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun logout() {
        user = null
        //todo
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    fun loginUser(username: String, password: String): MutableLiveData<LoginResult> {

        var data =  MutableLiveData<LoginResult>()
        val client = NetworkService.getInstance()
            ?.getApolloClient()

        val loginquery = LoginQuery(username, password)

        client
            ?.query(loginquery)
            ?.enqueue(object : ApolloCall.Callback<LoginQuery.Data>() {

                override fun onResponse(response: Response<LoginQuery.Data>) {
                    if (response.data?.login()?.error() == null) {
                        data.postValue(LoginResult(success = LoggedInUser(
                            displayName = response.data?.login()?.user()
                                ?.name()
                                .toString(),
                            userId = response.data?.login()?.user()?._id().toString(),
                            token = response.data?.login()?.token().toString()
                        )))
                    }
                    else
                    {
                        data.postValue(LoginResult(error = response.data?.login()?.error().toString()))
                    }                }

                override fun onFailure(e: ApolloException) {
                    data.postValue(LoginResult(error = e.toString()))
                }
            })
        return data
    }

    fun registerUser(username: String, password: String): MutableLiveData<LoginResult>{

        var data =  MutableLiveData<LoginResult>()
        val client = NetworkService.getInstance()
            ?.getApolloClient()

        val registerUserMutation = RegistrationMutation
            .builder()
            .name(username)
            .password(password)
            .build()

        client
        ?.mutate(registerUserMutation)
        ?.enqueue(object : ApolloCall.Callback<RegistrationMutation.Data>() {
            override fun onResponse(response: Response<RegistrationMutation.Data>) {
                if (response.data?.registration()?.error() == null) {
                    data.postValue(LoginResult(success = LoggedInUser(
                        displayName = response.data?.registration()?.user()
                            ?.name()
                            .toString(),
                        userId = response.data?.registration()?.user()?._id().toString(),
                        token = response.data?.registration()?.token().toString()
                    )))
                }
                else
                {
                    data.postValue(LoginResult(error = response.data?.registration()?.error().toString()))
                }
            }

            override fun onFailure(e: ApolloException) {
                data.value = LoginResult(error = e.toString())
            }
        })
        return data
    }
}
