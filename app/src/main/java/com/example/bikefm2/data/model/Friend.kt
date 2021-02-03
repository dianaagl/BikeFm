package com.example.bikefm2.data.model

import com.example.bikefm2.data.Location
import androidx.room.*


@Entity(tableName = "friend")
data class Friend(
    @PrimaryKey val userId: String,
    @ColumnInfo(name = "first_name")val displayName: String,
    @ColumnInfo(name = "last_name")val lastname: String,
    @Embedded
    val location: Location?,
    @ColumnInfo(name = "type")val type: String
)

enum class FriendTypes{
    friends,
    friendReqs,
    incomingFriendReq
}