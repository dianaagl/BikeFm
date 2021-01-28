package com.example.bikefm2.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.bikefm2.data.model.Friend
import com.example.bikefm2.data.model.User

@Database(entities = arrayOf(User::class, Friend::class), version = 1)
abstract class BikeDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    companion object DatabaseFact{
        @Volatile
        private var INSTANCE: BikeDatabase? = null
        fun getDatabase(context: Context): BikeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BikeDatabase::class.java,
                    "bike_database"
                )
//                    .addMigrations(MIGRATION_1_2)
//                    .addMigrations(MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
//        val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("ALTER TABLE LoggedInUser RENAME TO user")
//            }
//        }
//        val MIGRATION_2_3 = object : Migration(2, 3) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("CREATE TABLE friend (userId TEXT NOT NULL, first_name TEXT NOT NULL, last_name TEXT NOT NULL, PRIMARY KEY(userId))")
//            }
//        }
    }

}