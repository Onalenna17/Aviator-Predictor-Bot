package com.aviatorpredictorbot.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CrashRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrash(crash: CrashRecordEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrashes(crashes: List<CrashRecordEntity>)

    @Query("SELECT * FROM crash_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentCrashesFlow(limit: Int = 100): Flow<List<CrashRecordEntity>>

    @Query("SELECT * FROM crash_history ORDER BY timestamp DESC LIMIT 50")
    fun getLast50CrashesFlow(): Flow<List<CrashRecordEntity>>

    @Query("SELECT * FROM crash_history ORDER BY timestamp DESC LIMIT 50")
    suspend fun getLast50CrashesList(): List<CrashRecordEntity>

    @Query("SELECT * FROM crash_history ORDER BY timestamp DESC")
    fun getAllCrashesFlow(): Flow<List<CrashRecordEntity>>

    @Query("SELECT COUNT(*) FROM crash_history")
    fun getTotalCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM crash_history WHERE multiplier >= 10.0")
    fun getPinkCountFlow(): Flow<Int>

    @Query("SELECT AVG(multiplier) FROM crash_history")
    fun getAverageMultiplierFlow(): Flow<Double?>

    @Query("SELECT MAX(multiplier) FROM crash_history")
    fun getMaxMultiplierFlow(): Flow<Double?>

    /**
     * Database Layer Configuration: Automatically clears crash history older than cutoff timestamp (e.g. 7 days).
     */
    @Query("DELETE FROM crash_history WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteCrashesOlderThan(cutoffTimestamp: Long): Int

    @Query("SELECT COUNT(*) FROM crash_history WHERE timestamp < :cutoffTimestamp")
    suspend fun countCrashesOlderThan(cutoffTimestamp: Long): Int

    @Query("DELETE FROM crash_history")
    suspend fun clearAll()
}
