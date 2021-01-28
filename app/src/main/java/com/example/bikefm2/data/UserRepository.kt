package com.example.bikefm2.data

import android.util.Log
import com.example.bikefm2.BikeFmApp
import com.example.bikefm2.Network.NetworkApiCall
import com.example.bikefm2.db.BikeDatabase
import com.example.bikefm2.data.model.Friend
import com.example.bikefm2.data.model.User
import javax.inject.Inject


/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class UserRepository @Inject constructor(
    val serverApi: NetworkApiCall
) {
    private val mDatabase: BikeDatabase = BikeDatabase.getDatabase(BikeFmApp.applicationContext())
    private var cashedUser: User? = null

    fun getUser(): User?{
        cashedUser = mDatabase.userDao().getUser()
        return cashedUser
    }

    suspend fun verifyUser(): Result<User> {
        return cashedUser?.let {
            try {
                when (val resUser = serverApi.verifyUser(it.token)){
                    is Result.Success -> {
                        mDatabase.userDao().deleteAllFriends()
                        mDatabase.userDao().updateUser(resUser.data)
                        cashedUser = resUser.data
                        resUser
                    }
                    is Result.Error -> {
                        resUser
                    }
                }
            } catch (e: Exception) {
                Result.Error(exception = e)
            }
        } ?: Result.Error(exception = java.lang.Exception("no user"))
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

    suspend fun loginUser(username: String, password: String): Result<User>{

        try {
            return when(val loginRes = serverApi.loginUser(username, password)){
                is Result.Success -> {
                    mDatabase.userDao().deleteAll()
                    mDatabase.userDao().deleteAllFriends()
                    mDatabase.userDao().insert(loginRes.data)
                    loginRes.data.friends?.let { mDatabase.userDao().addFriends(it) }
                    setLoggedInUser(loginRes.data)
                    Result.Success(data = loginRes.data)
                }
                is Result.Error -> Result.Error(exception = loginRes.exception)
            }

        } catch(e: Exception) {
            return Result.Error(exception = e)
        }
    }

    suspend fun registerUser(username: String, password: String): Result<User>{

        try {
            return when(val loginRes = serverApi.registerUser(username, password)){
                is Result.Success -> {
                    mDatabase.userDao().deleteAll()
                    mDatabase.userDao().deleteAllFriends()
                    mDatabase.userDao().insert(loginRes.data)
                    setLoggedInUser(loginRes.data)
                    loginRes
                }
                is Result.Error -> {
                    loginRes
                }
            }
        } catch(e: Exception) {
            return Result.Error(exception = e)
        }
    }

    suspend fun findUser(username: String): Result<List<Friend>>{
        return try {
            cashedUser?.let { serverApi.findUsers(username, it.token)?.let { users -> Result.Success(data = users) } }
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
}
