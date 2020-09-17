package ziox.ramiro.saes.databases

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log

/**
 * Creado por Ramiro el 14/04/2019 a las 04:10 PM para SAESv2.
 */
class EstadoGeneralDatabase (context: Context?) : SQLiteOpenHelper(context, "datos_escolares.db", null,1){
    val col = DBCols()
    data class DBCols(val tableName : String = "reinscripcion",
                      val _id : String = "_id",
                      val tipo: String = "tipo",
                      val v1: String = "v1",
                      val v2: String = "v2",
                      val v3: String = "v3") : BaseColumns

    data class Data(val tipo: String,
                    val v1: String,
                    val v2: String,
                    val v3: String)

    companion object {
        fun cursorAsClaseData(cursor: Cursor) : Data{
            val col = DBCols()
            return Data(cursor.getString(cursor.getColumnIndex(col.tipo)),
                cursor.getString(cursor.getColumnIndex(col.v1)),
                cursor.getString(cursor.getColumnIndex(col.v2)),
                cursor.getString(cursor.getColumnIndex(col.v3)))
        }
    }

    fun createTable(){
        val p0 = writableDatabase
        try {
            p0.execSQL("CREATE TABLE " + col.tableName + " ("
                    + col._id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + col.tipo + " TEXT NOT NULL,"
                    + col.v1 + " TEXT NOT NULL,"
                    + col.v2 + " TEXT NOT NULL,"
                    + col.v3 + " TEXT NOT NULL)")
        }catch (e : Exception){
            Log.e("AppException", e.toString())
        }
    }

    fun deleteTable(){
        val p0 = writableDatabase

        try {
            p0.execSQL("DROP TABLE IF EXISTS "+col.tableName)
        }catch (e : Exception){
            Log.e("AppException", e.toString())
        }
    }

    fun addData(data : Data) : Boolean{
        val p0 = writableDatabase

        return try {
            p0.insert(col.tableName, null, toContentValues(data))
            true
        } catch (e: Exception) {
            Log.e("AppException", e.toString())
            false
        }

    }

    fun getAll() : Cursor {
        return readableDatabase.query(col.tableName,
            null,
            null,
            null,
            null,
            null,
            null)
    }

    override fun onCreate(p0: SQLiteDatabase?) {}
    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {}

    private fun toContentValues(vals : Data): ContentValues {
        val col = DBCols()
        val values = ContentValues()
        values.put(col.tipo, vals.tipo)
        values.put(col.v1, vals.v1)
        values.put(col.v2, vals.v2)
        values.put(col.v3, vals.v3)
        return values
    }
}