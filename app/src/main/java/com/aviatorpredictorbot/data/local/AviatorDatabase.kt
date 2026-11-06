package com.aviatorpredictorbot.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CrashRecordEntity::class], version = 1, exportSchema = false)
abstract class AviatorDatabase : RoomDatabase() {

    abstract fun crashRecordDao(): CrashRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AviatorDatabase? = null

        fun getInstance(context: Context): AviatorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AviatorDatabase::class.java,
                    "betway_aviator_crash_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
