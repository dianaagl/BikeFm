package com.example.bikefm2.data

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.bikefm2.BikeFmApp
import com.example.bikefm2.Network.NetworkApiCall
import com.example.bikefm2.db.BikeDatabase
import com.example.bikefm2.data.model.Friend
import com.example.bikefm2.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class UserRepository @Inject constructor(
    val serverApi: NetworkApiCall
) {
    private val mDatabase: BikeDatabase = BikeDatabase.getDatabase(BikeFmApp.applicationContext())
    // in-memory cache of the loggedInUser object
    var cashedUser: User? = null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        cashedUser = null
    }
    suspend fun getUser(): User?{
        return mDatabase.userDao().getUser()
    }
    suspend fun verifyUser(): LoginResult {
//        val job = BikeFmApp.getMapScope()?.launch {
            val dbUser = mDatabase.userDao().getUser()
            if (dbUser !== null) {
                return try {
                    val resUser: User? = serverApi.verifyUser(dbUser.token)
                    if (resUser !== null) {
                        mDatabase.userDao().deleteAllFriends()
                        mDatabase.userDao().updateUser(resUser)
                        cashedUser = resUser
                        LoginResult(success = resUser)
                    }
                    else
                    {
                        LoginResult(error = "no user")
                    }
                } catch (e: Exception) {
                    LoginResult(error = e.toString())
                }
            }
             return LoginResult(error = "no user")
    }

    suspend fun getFriendsList(): LiveData<List<Friend>> {
        return mDatabase.userDao().getFriends()
    }

    suspend fun addFriendsList(friends: List<Friend>){
        return mDatabase.userDao().addFriends(friends)
    }

    suspend fun setUserLocation(loc: Location) = withContext(Dispatchers.Default){
//        val job = BikeFmApp.getMapScope()?.launch {
            val dbUser = mDatabase.userDao().getUser()
            if (dbUser !== null) {
                try {
                    serverApi.setUserLocation(dbUser.token, loc)
                } catch (e: Exception) {
                    Log.e("UserRepo", e.toString())
                }
            }
    }

    private fun setLoggedInUser(user: User) {
        this.cashedUser = user
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    suspend fun loginUser(username: String, password: String): LoginResult = withContext(Dispatchers.IO){

        val result = try {
            val user = serverApi.loginUser(username, password)
            mDatabase.userDao().deleteAll()
            mDatabase.userDao().deleteAllFriends()
            mDatabase.userDao().insert(user)
            return@withContext LoginResult(success = user)
        } catch(e: Exception) {
            return@withContext LoginResult(error = e.toString())
        }
        return@withContext result
    }

    suspend fun registerUser(username: String, password: String): LoginResult = withContext(Dispatchers.IO){

        val result = try {
            val user = serverApi.registerUser(username, password)
            mDatabase.userDao().deleteAll()
            mDatabase.userDao().deleteAllFriends()
            mDatabase.userDao().insert(user)
            return@withContext LoginResult(success = user)
        } catch(e: Exception) {
            return@withContext LoginResult(error = e.toString())
        }
        return@withContext result
    }

    suspend fun findUser(username: String): List<Friend>{

        try {
            val user = serverApi.findUsers(username)
            if(user !== null)
                return user
            else
                throw(Exception("not found"))
        } catch(e: Exception) {
            return listOf<Friend>()
        }
    }

}
