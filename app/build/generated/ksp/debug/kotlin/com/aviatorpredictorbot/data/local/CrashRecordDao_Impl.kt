package com.aviatorpredictorbot.`data`.local

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.getTotalChangedRows
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class CrashRecordDao_Impl(
  __db: RoomDatabase,
) : CrashRecordDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCrashRecordEntity: EntityInsertAdapter<CrashRecordEntity>

  private val __insertAdapterOfCrashRecordEntity_1: EntityInsertAdapter<CrashRecordEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfCrashRecordEntity = object : EntityInsertAdapter<CrashRecordEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `crash_history` (`id`,`roundId`,`multiplier`,`timestamp`,`formattedTime`,`isPink`,`isMid`,`isLow`) VALUES (?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CrashRecordEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.roundId)
        statement.bindDouble(3, entity.multiplier)
        statement.bindLong(4, entity.timestamp)
        statement.bindText(5, entity.formattedTime)
        val _tmp: Int = if (entity.isPink) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        val _tmp_1: Int = if (entity.isMid) 1 else 0
        statement.bindLong(7, _tmp_1.toLong())
        val _tmp_2: Int = if (entity.isLow) 1 else 0
        statement.bindLong(8, _tmp_2.toLong())
      }
    }
    this.__insertAdapterOfCrashRecordEntity_1 = object : EntityInsertAdapter<CrashRecordEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR IGNORE INTO `crash_history` (`id`,`roundId`,`multiplier`,`timestamp`,`formattedTime`,`isPink`,`isMid`,`isLow`) VALUES (?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CrashRecordEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.roundId)
        statement.bindDouble(3, entity.multiplier)
        statement.bindLong(4, entity.timestamp)
        statement.bindText(5, entity.formattedTime)
        val _tmp: Int = if (entity.isPink) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        val _tmp_1: Int = if (entity.isMid) 1 else 0
        statement.bindLong(7, _tmp_1.toLong())
        val _tmp_2: Int = if (entity.isLow) 1 else 0
        statement.bindLong(8, _tmp_2.toLong())
      }
    }
  }

  public override suspend fun insertCrash(crash: CrashRecordEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfCrashRecordEntity.insert(_connection, crash)
  }

  public override suspend fun insertCrashes(crashes: List<CrashRecordEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfCrashRecordEntity_1.insert(_connection, crashes)
  }

  public override fun getRecentCrashesFlow(limit: Int): Flow<List<CrashRecordEntity>> {
    val _sql: String = "SELECT * FROM crash_history ORDER BY timestamp DESC LIMIT ?"
    return createFlow(__db, false, arrayOf("crash_history")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRoundId: Int = getColumnIndexOrThrow(_stmt, "roundId")
        val _columnIndexOfMultiplier: Int = getColumnIndexOrThrow(_stmt, "multiplier")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfFormattedTime: Int = getColumnIndexOrThrow(_stmt, "formattedTime")
        val _columnIndexOfIsPink: Int = getColumnIndexOrThrow(_stmt, "isPink")
        val _columnIndexOfIsMid: Int = getColumnIndexOrThrow(_stmt, "isMid")
        val _columnIndexOfIsLow: Int = getColumnIndexOrThrow(_stmt, "isLow")
        val _result: MutableList<CrashRecordEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CrashRecordEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpRoundId: String
          _tmpRoundId = _stmt.getText(_columnIndexOfRoundId)
          val _tmpMultiplier: Double
          _tmpMultiplier = _stmt.getDouble(_columnIndexOfMultiplier)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpFormattedTime: String
          _tmpFormattedTime = _stmt.getText(_columnIndexOfFormattedTime)
          val _tmpIsPink: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsPink).toInt()
          _tmpIsPink = _tmp != 0
          val _tmpIsMid: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsMid).toInt()
          _tmpIsMid = _tmp_1 != 0
          val _tmpIsLow: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLow).toInt()
          _tmpIsLow = _tmp_2 != 0
          _item =
              CrashRecordEntity(_tmpId,_tmpRoundId,_tmpMultiplier,_tmpTimestamp,_tmpFormattedTime,_tmpIsPink,_tmpIsMid,_tmpIsLow)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getLast50CrashesFlow(): Flow<List<CrashRecordEntity>> {
    val _sql: String = "SELECT * FROM crash_history ORDER BY timestamp DESC LIMIT 50"
    return createFlow(__db, false, arrayOf("crash_history")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRoundId: Int = getColumnIndexOrThrow(_stmt, "roundId")
        val _columnIndexOfMultiplier: Int = getColumnIndexOrThrow(_stmt, "multiplier")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfFormattedTime: Int = getColumnIndexOrThrow(_stmt, "formattedTime")
        val _columnIndexOfIsPink: Int = getColumnIndexOrThrow(_stmt, "isPink")
        val _columnIndexOfIsMid: Int = getColumnIndexOrThrow(_stmt, "isMid")
        val _columnIndexOfIsLow: Int = getColumnIndexOrThrow(_stmt, "isLow")
        val _result: MutableList<CrashRecordEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CrashRecordEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpRoundId: String
          _tmpRoundId = _stmt.getText(_columnIndexOfRoundId)
          val _tmpMultiplier: Double
          _tmpMultiplier = _stmt.getDouble(_columnIndexOfMultiplier)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpFormattedTime: String
          _tmpFormattedTime = _stmt.getText(_columnIndexOfFormattedTime)
          val _tmpIsPink: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsPink).toInt()
          _tmpIsPink = _tmp != 0
          val _tmpIsMid: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsMid).toInt()
          _tmpIsMid = _tmp_1 != 0
          val _tmpIsLow: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLow).toInt()
          _tmpIsLow = _tmp_2 != 0
          _item =
              CrashRecordEntity(_tmpId,_tmpRoundId,_tmpMultiplier,_tmpTimestamp,_tmpFormattedTime,_tmpIsPink,_tmpIsMid,_tmpIsLow)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLast50CrashesList(): List<CrashRecordEntity> {
    val _sql: String = "SELECT * FROM crash_history ORDER BY timestamp DESC LIMIT 50"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRoundId: Int = getColumnIndexOrThrow(_stmt, "roundId")
        val _columnIndexOfMultiplier: Int = getColumnIndexOrThrow(_stmt, "multiplier")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfFormattedTime: Int = getColumnIndexOrThrow(_stmt, "formattedTime")
        val _columnIndexOfIsPink: Int = getColumnIndexOrThrow(_stmt, "isPink")
        val _columnIndexOfIsMid: Int = getColumnIndexOrThrow(_stmt, "isMid")
        val _columnIndexOfIsLow: Int = getColumnIndexOrThrow(_stmt, "isLow")
        val _result: MutableList<CrashRecordEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CrashRecordEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpRoundId: String
          _tmpRoundId = _stmt.getText(_columnIndexOfRoundId)
          val _tmpMultiplier: Double
          _tmpMultiplier = _stmt.getDouble(_columnIndexOfMultiplier)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpFormattedTime: String
          _tmpFormattedTime = _stmt.getText(_columnIndexOfFormattedTime)
          val _tmpIsPink: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsPink).toInt()
          _tmpIsPink = _tmp != 0
          val _tmpIsMid: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsMid).toInt()
          _tmpIsMid = _tmp_1 != 0
          val _tmpIsLow: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLow).toInt()
          _tmpIsLow = _tmp_2 != 0
          _item =
              CrashRecordEntity(_tmpId,_tmpRoundId,_tmpMultiplier,_tmpTimestamp,_tmpFormattedTime,_tmpIsPink,_tmpIsMid,_tmpIsLow)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllCrashesFlow(): Flow<List<CrashRecordEntity>> {
    val _sql: String = "SELECT * FROM crash_history ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("crash_history")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRoundId: Int = getColumnIndexOrThrow(_stmt, "roundId")
        val _columnIndexOfMultiplier: Int = getColumnIndexOrThrow(_stmt, "multiplier")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfFormattedTime: Int = getColumnIndexOrThrow(_stmt, "formattedTime")
        val _columnIndexOfIsPink: Int = getColumnIndexOrThrow(_stmt, "isPink")
        val _columnIndexOfIsMid: Int = getColumnIndexOrThrow(_stmt, "isMid")
        val _columnIndexOfIsLow: Int = getColumnIndexOrThrow(_stmt, "isLow")
        val _result: MutableList<CrashRecordEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CrashRecordEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpRoundId: String
          _tmpRoundId = _stmt.getText(_columnIndexOfRoundId)
          val _tmpMultiplier: Double
          _tmpMultiplier = _stmt.getDouble(_columnIndexOfMultiplier)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpFormattedTime: String
          _tmpFormattedTime = _stmt.getText(_columnIndexOfFormattedTime)
          val _tmpIsPink: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsPink).toInt()
          _tmpIsPink = _tmp != 0
          val _tmpIsMid: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsMid).toInt()
          _tmpIsMid = _tmp_1 != 0
          val _tmpIsLow: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLow).toInt()
          _tmpIsLow = _tmp_2 != 0
          _item =
              CrashRecordEntity(_tmpId,_tmpRoundId,_tmpMultiplier,_tmpTimestamp,_tmpFormattedTime,_tmpIsPink,_tmpIsMid,_tmpIsLow)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getTotalCountFlow(): Flow<Int> {
    val _sql: String = "SELECT COUNT(*) FROM crash_history"
    return createFlow(__db, false, arrayOf("crash_history")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getPinkCountFlow(): Flow<Int> {
    val _sql: String = "SELECT COUNT(*) FROM crash_history WHERE multiplier >= 10.0"
    return createFlow(__db, false, arrayOf("crash_history")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAverageMultiplierFlow(): Flow<Double?> {
    val _sql: String = "SELECT AVG(multiplier) FROM crash_history"
    return createFlow(__db, false, arrayOf("crash_history")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Double?
        if (_stmt.step()) {
          val _tmp: Double?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getDouble(0)
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getMaxMultiplierFlow(): Flow<Double?> {
    val _sql: String = "SELECT MAX(multiplier) FROM crash_history"
    return createFlow(__db, false, arrayOf("crash_history")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Double?
        if (_stmt.step()) {
          val _tmp: Double?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getDouble(0)
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun countCrashesOlderThan(cutoffTimestamp: Long): Int {
    val _sql: String = "SELECT COUNT(*) FROM crash_history WHERE timestamp < ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, cutoffTimestamp)
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteCrashesOlderThan(cutoffTimestamp: Long): Int {
    val _sql: String = "DELETE FROM crash_history WHERE timestamp < ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, cutoffTimestamp)
        _stmt.step()
        getTotalChangedRows(_connection)
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAll() {
    val _sql: String = "DELETE FROM crash_history"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
