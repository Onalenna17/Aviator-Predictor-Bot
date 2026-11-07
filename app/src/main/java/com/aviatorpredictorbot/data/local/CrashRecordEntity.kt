package com.aviatorpredictorbot.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crash_history")
data class CrashRecordEntity(
    @PrimaryKey val id: String,
    val roundId: String,
    val multiplier: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedTime: String = "",
    val isPink: Boolean = multiplier >= 10.0,
    val isMid: Boolean = multiplier >= 2.0 && multiplier < 10.0,
    val isLow: Boolean = multiplier < 2.0
)
