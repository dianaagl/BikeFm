package com.example.bikefm2.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "friend")
data class Friend(
    @PrimaryKey val userId: String,
    @ColumnInfo(name = "first_name")val displayName: String,
    @ColumnInfo(name = "last_name")val lastname: String
)
