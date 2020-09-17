package ziox.ramiro.saes.databases

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

/**
 * Creado por Ramiro el 14/04/2019 a las 04:10 PM para SAESv2.
 */
class CalificacionesDatabase (context: Context?) : SQLiteOpenHelper(context, "datos_escolares.db", null,1){
    val col = DBCols()
    data class DBCols(val tableName : String = "califInscAct",
                      val _id : String = "_id",
                      val materia: String = "materia",
                      val grupo: String = "grupo",
                      val p1: String = "p1",
                      val p2: String = "p2",
                      val p3: String = "p3",
                      val extra: String = "extra",
                      val final: String = "final") : BaseColumns

    data class Data(val materia: String,
                    val grupo: String,
                    val p1: String,
                    val p2: String,
                    val p3: String,
                    val extra: String,
                    val final: String)

    companion object {
        fun cursorAsClaseData(cursor: Cursor) : Data{
            val col = DBCols()
            return Data(cursor.getString(cursor.getColumnIndex(col.materia)),
                cursor.getString(cursor.getColumnIndex(col.grupo)),
                cursor.getString(cursor.getColumnIndex(col.p1)),
                cursor.getString(cursor.getColumnIndex(col.p2)),
                cursor.getString(cursor.getColumnIndex(col.p3)),
                cursor.getString(cursor.getColumnIndex(col.extra)),
                cursor.getString(cursor.getColumnIndex(col.final)))
        }
    }

    fun createTable(){
        val p0 = writableDatabase
        try {
            p0.execSQL("CREATE TABLE " + col.tableName + " ("
                    + col._id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + col.materia + " TEXT NOT NULL,"
                    + col.grupo + " TEXT NOT NULL,"
                    + col.p1 + " TEXT NOT NULL,"
                    + col.p2 + " TEXT NOT NULL,"
                    + col.p3 + " TEXT NOT NULL,"
                    + col.extra + " TEXT NOT NULL,"
                    + col.final + " TEXT NOT NULL)")
        }catch (e : Exception){

        }
    }

    fun deleteTable(){
        try {
            val p0 = writableDatabase
            p0.execSQL("DROP TABLE IF EXISTS "+col.tableName)
        }catch (e : Exception){

        }
    }

    fun addMateria(data : Data) : Boolean{
        val p0 = writableDatabase

        return try {
            p0.insert(col.tableName, null, toContentValues(data))
            true
        } catch (e: Exception) {
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
        values.put(col.materia, vals.materia)
        values.put(col.grupo, vals.grupo)
        values.put(col.p1, vals.p1)
        values.put(col.p2, vals.p2)
        values.put(col.p3, vals.p3)
        values.put(col.extra, vals.extra)
        values.put(col.final, vals.final)

        return values
    }
}