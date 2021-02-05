package com.example.bikefm2.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.bikefm2.BikeFmApp
import com.example.bikefm2.network.NetworkApiCall
import com.example.bikefm2.db.BikeDatabase
import com.example.bikefm2.data.model.Friend
import com.example.bikefm2.data.model.User
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
@Singleton
class UserRepository @Inject constructor(
    val serverApi: NetworkApiCall
) {
    private val mDatabase: BikeDatabase = BikeDatabase.getDatabase(BikeFmApp.applicationContext())
    private val _user = MutableLiveData<Result<User>>()
    val user: LiveData<Result<User>> = _user
    private var cashedUser: User? = null

    fun getUser(){
        mDatabase.userDao().getUser()?.let {
            it.friends = mDatabase.userDao().getFriends().value
            it.friendsRequests = mDatabase.userDao().getFriendRequests().value
            it.sentFriendsRequests = mDatabase.userDao().getSentFriendRequests().value

            _user.postValue(Result.Success(it))
            cashedUser = it
        } ?: _user.postValue(Result.Error(java.lang.Exception("no user")))
    }

    fun logout(){
        cashedUser = null
        mDatabase.userDao().deleteAll()
        mDatabase.userDao().deleteAllFriends()
    }

    suspend fun verifyUser(){
        cashedUser?.let {
            try {
                val resUser = serverApi.verifyUser(it.token)
                when (resUser){
                    is Result.Success -> {
                        mDatabase.userDao().deleteAllFriends()
                        mDatabase.userDao().updateUser(resUser.data)
                        mDatabase.userDao().addFriends(resUser.data.friends?: listOf())
                        mDatabase.userDao().addFriends(resUser.data.friendsRequests?: listOf())
                        mDatabase.userDao().addFriends(resUser.data.sentFriendsRequests?: listOf())
                        cashedUser = resUser.data
                    }
                    is Result.Error -> {
                        cashedUser = null
                        mDatabase.userDao().deleteAllFriends()
                        mDatabase.userDao().deleteAll()
                    }
                }
                _user.postValue(resUser)
            } catch (e: Exception) {
                _user.postValue(Result.Error(exception = e))
            }
        } ?: _user.postValue(Result.Error(exception = java.lang.Exception("no user")))
    }

    suspend fun setUserLocation(loc: Location) {
        cashedUser?.let {
            try {
                serverApi.setUserLocation(it.token, loc)
            } catch (e: Exception) {
                Log.e("UserRepo", e.toString())
            }
        }
    }

    private fun setLoggedInUser(user: User) {
        this.cashedUser = user
    }

    suspend fun loginUser(username: String, password: String){

        try {
            val loginRes = serverApi.loginUser(username, password)
            when(loginRes){
                is Result.Success -> {
                    mDatabase.userDao().deleteAll()
                    mDatabase.userDao().deleteAllFriends()
                    mDatabase.userDao().insert(loginRes.data)
                    loginRes.data.friends?.let { mDatabase.userDao().addFriends(it) }
                    setLoggedInUser(loginRes.data)
                }
                is Result.Error -> {
                }
            }
            _user.postValue(loginRes)

        } catch(e: Exception) {
            return  _user.postValue(Result.Error(exception = e))
        }
    }

    suspend fun registerUser(username: String, password: String){
        try {
            val loginRes = serverApi.registerUser(username, password)
            when(loginRes) {
                is Result.Success -> {
                    mDatabase.userDao().deleteAll()
                    mDatabase.userDao().deleteAllFriends()
                    mDatabase.userDao().insert(loginRes.data)
                    setLoggedInUser(loginRes.data)
                }
                is Result.Error -> {
                }
            }
            _user.postValue(loginRes)
        } catch(e: Exception) {
            _user.postValue(Result.Error(exception = e))
        }
    }

    suspend fun findUser(username: String): Result<List<Friend>>{
        return try {
            cashedUser?.let {
                serverApi.findUsers(username, it.token)?.let { users -> Result.Success(data = users) } }
                ?: Result.Success(data = listOf())
        } catch (e: Exception) {
            Result.Error(exception = e)
        }
    }

    suspend fun addFriend(userId: String) {
        try{
            cashedUser?.let { serverApi.addFriend(userId, it.token) }

        } catch (e: Exception) {
            Log.e("error", e.toString())
        }
    }

    suspend fun confirmFriendship(userId: String) {
        try{
            cashedUser?.let { serverApi.confirmFriendship(userId, it.token) }

        } catch (e: Exception) {
            Log.e("error", e.toString())
        }
    }
}
