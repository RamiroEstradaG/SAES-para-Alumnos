package ziox.ramiro.saes.databases

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import androidx.room.*

/**
 * Nombre de la tabla del Kardex
 */
const val KARDEX_TABLE_NAME = "kardex"

/**
 * Entidad para una clase mostrada en el Kardex
 */
@Entity(tableName = KARDEX_TABLE_NAME)
data class KardexClass(
    @ColumnInfo(name = "course_name") val courseName: String,
    @ColumnInfo(name = "semester") val semester: String,
    @ColumnInfo(name = "final_score") val finalScore: String,
    @PrimaryKey(autoGenerate = true) val uid : Int = 0
)

/**
 * DAO del Kardex
 */
@Dao
interface KardexDao{
    /**
     * Obtiene todas las clases de la base de datos
     */
    @Query("SELECT * FROM $KARDEX_TABLE_NAME")
    fun getAll() : List<KardexClass>

    /**
     * Inserta un elemento a la base de datos
     */
    @Insert
    fun insert(clase : KardexClass)

    /**
     * Inserta N elementos a la base de datos
     */
    @Insert
    fun insertAll(vararg clases : KardexClass)

    /**
     * Elimina todos los elementos de la base de datos
     */
    @Query("DELETE FROM $KARDEX_TABLE_NAME")
    fun deleteAll()
}

/**
 * Creado por Ramiro el 12/4/2018 a las 8:20 PM para SAESv2.
 * @deprecated
 */
//class KardexDatabase (context: Context?) : SQLiteOpenHelper(context, "datos_escolares.db", null,1){
//    val col = DBCols()
//    data class DBCols(val tableName : String = "kardex",
//                      val _id : String = "_id",
//                      val name: String = "materia",
//                      val semestre: String = "semestre",
//                      val calificacion: String = "calificacion") : BaseColumns
//
//    data class Data(val name: String,
//                    val semestre: String,
//                    val calificacion: String)
//
//
//    companion object {
//        fun cursorAsData(cursor: Cursor) : Data{
//            val col = DBCols()
//            return Data(cursor.getString(cursor.getColumnIndex(col.name)),
//                cursor.getString(cursor.getColumnIndex(col.semestre)),
//                cursor.getString(cursor.getColumnIndex(col.calificacion)))
//        }
//    }
//
//    fun createTable(){
//        val p0 = writableDatabase
//        try {
//            p0.execSQL("CREATE TABLE " + col.tableName + " ("
//                    + col._id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
//                    + col.name + " TEXT NOT NULL,"
//                    + col.semestre + " TEXT NOT NULL,"
//                    + col.calificacion + " TEXT NOT NULL)")
//        }catch (e : Exception){
//            Log.e(this.javaClass.canonicalName, e.toString())
//        }
//    }
//
//    fun deleteTable(){
//        val p0 = writableDatabase
//
//        try {
//            p0.execSQL("DROP TABLE IF EXISTS "+col.tableName)
//        }catch (e : Exception){
//            Log.e(this.javaClass.canonicalName, e.toString())
//        }
//    }
//
//    fun addMateria(data : Data) : Boolean{
//        val p0 = writableDatabase
//
//        return try {
//            p0.insert(col.tableName, null, toContentValues(data))
//            true
//        } catch (e: Exception) {
//            Log.e(this.javaClass.canonicalName, e.toString())
//            false
//        }
//
//    }
//
//    fun getAll() : Cursor {
//        return readableDatabase.query(col.tableName,
//            null,
//            null,
//            null,
//            null,
//            null,
//            null)
//    }
//
//    override fun onCreate(p0: SQLiteDatabase?) {}
//    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {}
//
//    private fun toContentValues(vals : Data): ContentValues {
//        val col = DBCols()
//        val values = ContentValues()
//        values.put(col.name, vals.name)
//        values.put(col.semestre, vals.semestre)
//        values.put(col.calificacion, vals.calificacion)
//
//        return values
//    }
//}