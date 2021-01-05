package ziox.ramiro.saes.databases

import androidx.room.*
import ziox.ramiro.saes.fragments.OverallStatusFragment

/**
 * Nombre de la tabla
 */
const val OVERALL_STATUS_TABLE_NAME = "overall_status"

/**
 * Entidad de una clase que se encuentra en el estado general
 */
@Entity(tableName = OVERALL_STATUS_TABLE_NAME)
data class CourseStatus(
    @ColumnInfo(name = "course_name") val courseName : String,
    @ColumnInfo(name = "status_type") val statusType: Int,
    @PrimaryKey(autoGenerate = true) val uid : Int = 0
)

/**
 * DAO del estado general
 * @todo Implementar en [OverallStatusFragment]
 */
@Dao
interface OverallStatusDao{
    /**
     * Obtiene todas las clases de la base de datos
     */
    @Query("SELECT * FROM $OVERALL_STATUS_TABLE_NAME")
    fun getAll() : List<CourseStatus>

    /**
     * Inserta un elemento a la base de datos
     */
    @Insert
    fun insert(courseStatus : CourseStatus)

    /**
     * Inserta N elementos a la base de datos
     */
    @Insert
    fun insertAll(vararg courseStatuses : CourseStatus)

    /**
     * Elimina todos los elementos de la base de datos
     */
    @Query("DELETE FROM $OVERALL_STATUS_TABLE_NAME")
    fun deleteAll()
}