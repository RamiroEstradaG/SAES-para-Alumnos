package ziox.ramiro.saes.databases

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import androidx.room.*

const val RE_ENROLLMENT_TABLE_NAME = "re_enrollment"
const val TYPE_APPOINTMENT_DATE = "APPOINTMENT_DATE"
const val TYPE_LOAD = "LOAD"
const val TYPE_PERIOD = "PERIOD"
const val TYPE_CREDITS = "CREDITS"

@Entity(tableName = RE_ENROLLMENT_TABLE_NAME)
data class ReEnrollmentData(
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "value1") val value1: String,
    @ColumnInfo(name = "value2") val value2: String,
    @ColumnInfo(name = "value3") val value3: String,
    @PrimaryKey(autoGenerate = true) val uid : Int = 0
)

@Dao
interface ReEnrollmentDao{
    @Query("SELECT * FROM $RE_ENROLLMENT_TABLE_NAME")
    fun getAll() : List<ReEnrollmentData>

    @Insert
    fun insert(reEnrollmentData: ReEnrollmentData)

    @Delete
    fun delete(reEnrollmentData: ReEnrollmentData)

    @Query("DELETE FROM $RE_ENROLLMENT_TABLE_NAME")
    fun deleteAll()
}