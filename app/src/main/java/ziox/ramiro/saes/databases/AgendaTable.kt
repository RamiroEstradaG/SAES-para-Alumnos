package ziox.ramiro.saes.databases

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import androidx.room.*

const val AGENDA_TABLE_NAME = "agenda"
const val TYPE_AGENDA_ESCOLAR = 0
const val TYPE_CALENDARIO_TRABAJO = 1

@Entity(tableName = AGENDA_TABLE_NAME)
data class AgendaEvent(
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "event_type") val eventType: String,
    @ColumnInfo(name = "group") val group: String,
    @ColumnInfo(name = "start") val start: String,
    @ColumnInfo(name = "finish") val finish: String,
    @ColumnInfo(name = "working_day") val isWorkingDay: Boolean,
    @PrimaryKey(autoGenerate = true) val uid : Int = 0
)

@Dao
interface AgendaDao {
    @Query("SELECT * FROM $AGENDA_TABLE_NAME")
    fun getAll() : List<AgendaEvent>

    @Query("SELECT * FROM $AGENDA_TABLE_NAME WHERE type = :type")
    fun getAllOfType(type: Int) : List<AgendaEvent>

    @Query("SELECT * FROM $AGENDA_TABLE_NAME WHERE `group` = :group")
    fun getAllOfGroup(group: String) : List<AgendaEvent>

    @Query("DELETE FROM $AGENDA_TABLE_NAME WHERE type = :type")
    fun deleteAllOfType(type: Int)

    @Query("DELETE FROM $AGENDA_TABLE_NAME WHERE `group` = :group")
    fun deleteAllOfGroup(group: String)

    @Insert
    fun insert(agendaEvent: AgendaEvent)

    @Delete
    fun delete(agendaEvent: AgendaEvent)

    @Query("DELETE FROM $AGENDA_TABLE_NAME")
    fun deleteAll()
}