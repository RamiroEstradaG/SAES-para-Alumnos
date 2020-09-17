package ziox.ramiro.saes.databases

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import ziox.ramiro.saes.utils.ClaseData

/**
 * Creado por Ramiro el 12/4/2018 a las 8:20 PM para SAESv2.
 */

class CorreccionHorarioDatabase (context: Context?) : SQLiteOpenHelper(context, "horario.db", null,1) {
    /**
     * id: string
     * dia: integer
     * nombre: string
     * hora-isInit: time
     * hora-termino: time
     * color: string
     * grupo: string
     * profesor: string
     * edificio: string
     * salon: string
     */

    init {
        this.createTable()
    }

    companion object {
        fun cursorAsClaseData(cursor: Cursor) : ClaseData{
            val col = DBCols()
            return ClaseData(cursor.getString(cursor.getColumnIndex(col.id)),
                cursor.getInt(cursor.getColumnIndex(col.diaIndex)),
                cursor.getString(cursor.getColumnIndex(col.materia)),
                cursor.getDouble(cursor.getColumnIndex(col.horaInicio)),
                cursor.getDouble(cursor.getColumnIndex(col.horaFinal)),
                cursor.getString(cursor.getColumnIndex(col.color)),
                cursor.getString(cursor.getColumnIndex(col.grupo)),
                cursor.getString(cursor.getColumnIndex(col.profesor)),
                cursor.getString(cursor.getColumnIndex(col.edificio)),
                cursor.getString(cursor.getColumnIndex(col.salon)))
        }
    }

    val col = DBCols()
    data class DBCols(val tableName : String = "correcciones",
                      val _id : String = "_id",
                      val id : String = "id",
                      val diaIndex: String = "diaIndex",
                      val materia: String = "nombre",
                      val horaInicio: String = "horaInicio",
                      val horaFinal: String = "horaFinal",
                      val color: String = "color",
                      val grupo: String = "grupo",
                      val profesor : String = "profesor",
                      val edificio : String = "edificio",
                      val salon: String = "salon") : BaseColumns

    override fun onCreate(db: SQLiteDatabase?) {}

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

    fun createTable(){
        try {
            writableDatabase.execSQL("CREATE TABLE " + col.tableName + " ("
                    + col._id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + col.id + " TEXT NOT NULL,"
                    + col.diaIndex + " INT NOT NULL,"
                    + col.materia + " TEXT NOT NULL,"
                    + col.horaInicio + " TEXT NOT NULL,"
                    + col.horaFinal + " TEXT NOT NULL,"
                    + col.color + " TEXT NOT NULL,"
                    + col.grupo + " TEXT NOT NULL,"
                    + col.profesor + " TEXT NOT NULL,"
                    + col.edificio + " TEXT NOT NULL,"
                    + col.salon + " TEXT NOT NULL)")
        }catch (e : Exception){
            Log.e("AppException", e.toString())
        }
    }

    fun deleteTable() : Boolean{
        return try {
            writableDatabase.execSQL("DROP TABLE IF EXISTS "+col.tableName)
            true
        }catch (e : Exception){
            Log.e("AppException", e.toString())
            false
        }
    }

    fun add(data : ClaseData) : Boolean {
        return try {
            writableDatabase.insert(col.tableName, null, toContentValues(data))
            true
        } catch (e: Exception) {
            Log.e("AppException", e.toString())
            false
        }
    }

    fun searchData(data: ClaseData?) : ClaseData?{
        val cursor = getAll()

        if(data != null){
            while(cursor.moveToNext()) {
                val cursorData = cursorAsClaseData(cursor)

                if(cursorData.id == data.id) {
                    cursor.close()
                    return cursorData
                }
            }
        }

        cursor.close()
        return null
    }

    fun deleteData(data: ClaseData) : Boolean{
        return try {
            writableDatabase.delete(col.tableName, col.id+" = '"+data.id+"'", null)
            true
        }catch (e : Exception) {
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

    private fun toContentValues(vals : ClaseData): ContentValues {
        val values = ContentValues()
        values.put(col.id, vals.id)
        values.put(col.materia, vals.materia)
        values.put(col.horaInicio, vals.horaInicio)
        values.put(col.horaFinal, vals.horaFinal)
        values.put(col.diaIndex, vals.diaIndex)
        values.put(col.grupo, vals.grupo)
        values.put(col.color, vals.color)
        values.put(col.profesor, vals.profesor)
        values.put(col.edificio, vals.edificio)
        values.put(col.salon, vals.salon)

        return values
    }
}