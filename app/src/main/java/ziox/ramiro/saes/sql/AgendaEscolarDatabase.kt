package ziox.ramiro.saes.sql

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

/**
 * Creado por Ramiro el 14/04/2019 a las 04:10 PM para SAESv2.
 */
class AgendaEscolarDatabase (context: Context?) : SQLiteOpenHelper(context, "datos_escolares.db", null,1){
    val col = DBCols()
    data class DBCols(val tableName : String = "agendaEscolarv4",
                      val _id : String = "_id",
                      val nombre: String = "nombre",
                      val type: String = "type",
                      val tipoEvento: String = "tipoEvento",
                      val grupo: String = "grupo",
                      val inicio: String = "inicio",
                      val final: String = "final",
                      val laborable: String = "laborable") : BaseColumns

    data class Data(val nombre: String,
                    val type: Int,
                    val tipoEvento: String,
                    val grupo: String,
                    val inicio: String,
                    val final: String,
                    val laborable: Int)

    companion object {
        const val TYPE_AGENDA_ESCOLAR = 0
        const val TYPE_CALENDARIO_TRABAJO = 1

        fun cursorAsClaseData(cursor: Cursor) : Data{
            val col = DBCols()
            return Data(cursor.getString(cursor.getColumnIndex(col.nombre)),
                cursor.getInt(cursor.getColumnIndex(col.type)),
                cursor.getString(cursor.getColumnIndex(col.tipoEvento)),
                cursor.getString(cursor.getColumnIndex(col.grupo)),
                cursor.getString(cursor.getColumnIndex(col.inicio)),
                cursor.getString(cursor.getColumnIndex(col.final)),
                cursor.getInt(cursor.getColumnIndex(col.laborable)))
        }
    }

    init {
        this.createTable()
    }

    fun createTable(){
        val p0 = writableDatabase
        try {
            p0.execSQL("CREATE TABLE " + col.tableName + " ("
                    + col._id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + col.nombre + " TEXT NOT NULL,"
                    + col.type + " INTEGER NOT NULL,"
                    + col.tipoEvento + " TEXT NOT NULL,"
                    + col.grupo + " TEXT NOT NULL,"
                    + col.inicio + " TEXT NOT NULL,"
                    + col.final + " TEXT NOT NULL,"
                    + col.laborable + " INTEGER NOT NULL)")
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

    fun addEvento(data : Data) : Boolean{
        val p0 = writableDatabase

        return try {
            p0.insert(col.tableName, null, toContentValues(data))
            true
        } catch (e: Exception) {
            false
        }
    }

    fun removeOnlyType(type: Int) : Boolean{
        val p0 = writableDatabase

        return try {
            p0.delete(col.tableName, "${col.type} = ?", arrayOf(type.toString()))
            true
        } catch (e: Exception) {
            false
        }
    }

    fun removeOnlyGrupo(grupo: String) : Boolean{
        val p0 = writableDatabase

        return try {
            p0.delete(col.tableName, "${col.grupo} = ?", arrayOf(grupo))
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

    fun getOnlyType(type: Int) : Cursor {
        return readableDatabase.query(col.tableName,
            null,
            "${col.type} = ?", arrayOf(type.toString()),
            null,
            null,
            null)
    }

    override fun onCreate(p0: SQLiteDatabase?) {}
    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {}

    private fun toContentValues(vals : Data): ContentValues {
        val col = DBCols()
        val values = ContentValues()
        values.put(col.nombre, vals.nombre)
        values.put(col.type, vals.type)
        values.put(col.tipoEvento, vals.tipoEvento)
        values.put(col.grupo, vals.grupo)
        values.put(col.inicio, vals.inicio)
        values.put(col.final, vals.final)
        values.put(col.laborable, vals.laborable)

        return values
    }
}