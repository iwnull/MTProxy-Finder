package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ProxyEntity

@Database(entities = [ProxyEntity::class], version = 1, exportSchema = false)
abstract class ProxyDatabase : RoomDatabase() {
    abstract fun proxyDao(): ProxyDao

    companion object {
        @Volatile
        private var INSTANCE: ProxyDatabase? = null

        fun getDatabase(context: Context): ProxyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProxyDatabase::class.java,
                    "proxy_checker_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
