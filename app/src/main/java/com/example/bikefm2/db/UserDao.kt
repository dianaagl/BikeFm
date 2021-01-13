package com.example.bikefm2.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.bikefm2.data.model.Friend
import com.example.bikefm2.data.model.User

@Dao
interface UserDao {
    /*
    *   get User who logged in system
    */
    @Query("SELECT * FROM user LIMIT 1")
    fun getUser(): User

    /*
    *   Log in User
    */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(user: User)
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
    fun updateUser(users: User)
    /*
*   get users friends
*/
    @Query("SELECT * FROM friend")
    fun getFriends(): LiveData<List<Friend>>

    /*

     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun addFriends(objects: List<Friend>)
}