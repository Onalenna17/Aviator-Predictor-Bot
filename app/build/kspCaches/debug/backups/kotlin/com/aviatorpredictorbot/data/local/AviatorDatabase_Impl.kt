package com.aviatorpredictorbot.`data`.local

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AviatorDatabase_Impl : AviatorDatabase() {
  private val _crashRecordDao: Lazy<CrashRecordDao> = lazy {
    CrashRecordDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1,
        "cb32c3f4d90e608544d6b0640a3600af", "56ca579eba9b9c12b2a4ebcc875be390") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `crash_history` (`id` TEXT NOT NULL, `roundId` TEXT NOT NULL, `multiplier` REAL NOT NULL, `timestamp` INTEGER NOT NULL, `formattedTime` TEXT NOT NULL, `isPink` INTEGER NOT NULL, `isMid` INTEGER NOT NULL, `isLow` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'cb32c3f4d90e608544d6b0640a3600af')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `crash_history`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsCrashHistory: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCrashHistory.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCrashHistory.put("roundId", TableInfo.Column("roundId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCrashHistory.put("multiplier", TableInfo.Column("multiplier", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCrashHistory.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCrashHistory.put("formattedTime", TableInfo.Column("formattedTime", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCrashHistory.put("isPink", TableInfo.Column("isPink", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCrashHistory.put("isMid", TableInfo.Column("isMid", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCrashHistory.put("isLow", TableInfo.Column("isLow", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCrashHistory: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCrashHistory: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoCrashHistory: TableInfo = TableInfo("crash_history", _columnsCrashHistory,
            _foreignKeysCrashHistory, _indicesCrashHistory)
        val _existingCrashHistory: TableInfo = read(connection, "crash_history")
        if (!_infoCrashHistory.equals(_existingCrashHistory)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |crash_history(com.aviatorpredictorbot.data.local.CrashRecordEntity).
              | Expected:
              |""".trimMargin() + _infoCrashHistory + """
              |
              | Found:
              |""".trimMargin() + _existingCrashHistory)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "crash_history")
  }

  public override fun clearAllTables() {
    super.performClear(false, "crash_history")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(CrashRecordDao::class, CrashRecordDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun crashRecordDao(): CrashRecordDao = _crashRecordDao.value
}
