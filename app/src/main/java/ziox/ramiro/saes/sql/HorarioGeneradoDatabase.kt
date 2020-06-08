package ziox.ramiro.saes.sql

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

/**
 * Creado por Ramiro el 1/21/2019 a las 11:17 PM para SAESv2.
 */

class HorarioGeneradoDatabase (context: Context?) : SQLiteOpenHelper(context, "horario.db", null,1) {
    companion object {
        fun cursorAsClaseData(cursor: Cursor) : Data {
            val col = DBCols()
            return Data(cursor.getString(cursor.getColumnIndex(col.carrera)),
                cursor.getInt(cursor.getColumnIndex(col.semestre)),
                cursor.getString(cursor.getColumnIndex(col.grupo)),
                cursor.getString(cursor.getColumnIndex(col.materia)),
                cursor.getString(cursor.getColumnIndex(col.profesor)),
                cursor.getString(cursor.getColumnIndex(col.edificio)),
                cursor.getString(cursor.getColumnIndex(col.salon)),
                cursor.getString(cursor.getColumnIndex(col.lunes)),
                cursor.getString(cursor.getColumnIndex(col.martes)),
                cursor.getString(cursor.getColumnIndex(col.miercoles)),
                cursor.getString(cursor.getColumnIndex(col.jueves)),
                cursor.getString(cursor.getColumnIndex(col.viernes)))
        }
    }

    data class Data(val carrera : String,
                    val semestre : Int,
                    val grupo : String,
                    val materia: String,
                    val profesor : String,
                    val edificio : String,
                    val salon : String,
                    val lunes : String,
                    val martes : String,
                    val miercoles : String,
                    val jueves : String,
                    val viernes : String)

    val col = DBCols()

    data class DBCols(val tableName : String = "horario_generado",
                      val _id : String = "_id",
                      val carrera : String = "carrera",
                      val semestre : String = "semestre",
                      val grupo : String = "grupo",
                      val profesor : String = "profesor",
                      val edificio : String = "edificio",
                      val salon : String = "salon",
                      val materia : String = "materia",
                      val lunes : String = "lunes",
                      val martes : String = "martes",
                      val miercoles : String = "miercoles",
                      val jueves : String = "jueves",
                      val viernes : String = "viernes") : BaseColumns

    override fun onCreate(p0: SQLiteDatabase?) {}

    fun createTable(){
        val p0 = writableDatabase
        try {
            p0.execSQL("CREATE TABLE " + col.tableName + " ("
                    + col._id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + col.carrera + " TEXT NOT NULL,"
                    + col.semestre + " INTEGER NOT NULL,"
                    + col.grupo + " TEXT NOT NULL,"
                    + col.materia + " TEXT NOT NULL,"
                    + col.profesor + " TEXT NOT NULL,"
                    + col.edificio + " TEXT NOT NULL,"
                    + col.salon + " TEXT NOT NULL,"
                    + col.lunes + " TEXT,"
                    + col.martes + " TEXT,"
                    + col.miercoles + " TEXT,"
                    + col.jueves + " TEXT,"
                    + col.viernes + " TEXT)")
        }catch (e : Exception){

        }
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {}

    fun deleteMateriaByName(name : String) : Boolean{
        return try {
            writableDatabase.execSQL("DELETE FROM "+ col.tableName +" WHERE materia = '"+name+"';")
            true
        }catch (e : Exception){
            false
        }
    }

    fun deleteTable(){
        try {
            writableDatabase.execSQL("DROP TABLE IF EXISTS "+col.tableName)
        }catch (e : Exception){

        }
    }

    fun add(data : Data) : Boolean {
        return try {
            writableDatabase.insert(col.tableName, null, toContentValues(data))
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

    fun getMateriaByName(name : String) : Cursor {
        return readableDatabase.query(col.tableName,
            null,
            col.materia + " = '$name'",
            null, null, null, null)
    }

    private fun toContentValues(vals : Data): ContentValues {
        val col = DBCols()
        val values = ContentValues()
        values.put(col.carrera, vals.carrera)
        values.put(col.semestre, vals.semestre)
        values.put(col.grupo, vals.grupo)
        values.put(col.materia, vals.materia)
        values.put(col.profesor, vals.profesor)
        values.put(col.edificio, vals.edificio)
        values.put(col.salon, vals.salon)
        values.put(col.lunes, vals.lunes)
        values.put(col.martes, vals.martes)
        values.put(col.miercoles, vals.miercoles)
        values.put(col.jueves, vals.jueves)
        values.put(col.viernes, vals.viernes)

        return values
    }
}