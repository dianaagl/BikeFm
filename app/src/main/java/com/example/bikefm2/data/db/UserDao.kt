package com.example.bikefm2.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.bikefm2.data.model.Friend
import com.example.bikefm2.data.model.LoggedInUser
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    /*
    *   get User who logged in system
    */
    @Query("SELECT * FROM user LIMIT 1")
    fun getUser(): LoggedInUser

    /*
    *   Log in User
    */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(user: LoggedInUser)
    /*
    *  Logout
    */
    @Query("DELETE FROM user")
    fun deleteAll()
    /*
    *  Logout
    */
    @Query("DELETE FROM friend")
    fun deleteAllFriends()
    /*
    Update userInfo
     */
    @Update
    fun updateUser(users: LoggedInUser)
    /*
*   get users friends
*/
    @Query("SELECT * FROM friend")
    fun getFriends(): LiveData<List<Friend>>

    /*

     */
    @Insert
    @JvmSuppressWildcards
    fun addFriends(objects: List<Friend>)
}