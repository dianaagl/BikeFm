package com.example.bikefm2.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
@Entity(tableName = "user")
data class User(
    @PrimaryKey val userId: String,
    @ColumnInfo(name = "first_name") val displayName: String,
    @ColumnInfo(name = "token")val token: String,
    @Ignore var friendsList: List<Friend>
){
    constructor(userId: String, displayName: String, token: String) : this(userId, displayName, token, listOf<Friend>()){

    }

}