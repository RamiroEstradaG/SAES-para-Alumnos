package ziox.ramiro.saes.databases

import androidx.room.*

/**
 * Nombre de la tabla para las calificaciones
 */
const val GRADES_TABLE_NAME = "current_semester_grades"

/**
 * Entidad para cada materia y sus calificaciones
 */
@Entity(tableName = GRADES_TABLE_NAME)
data class CourseGrade(
    @ColumnInfo(name = "course_name") val courseName : String,
    @ColumnInfo(name = "group") val group : String,
    @ColumnInfo(name = "partial_one") val partialOne : String,
    @ColumnInfo(name = "partial_two") val partialTwo : String,
    @ColumnInfo(name = "partial_three") val partialThree : String,
    @ColumnInfo(name = "extraordinary") val extraordinary : String,
    @ColumnInfo(name = "final_score") val finalScore : String,
    @PrimaryKey(autoGenerate = true) val uid : Int = 0
)

/**
 * DAO de las calificaciones
 */
@Dao
interface GradesDao{
    /**
     * Obtiene todos los elementos de la base de datos
     */
    @Query("SELECT * FROM $GRADES_TABLE_NAME")
    fun getAll() : List<CourseGrade>

    /**
     * Inserta un elemento a la base de datos
     * @param courseGrade Elemento a insertar
     */
    @Insert
    fun insert(courseGrade: CourseGrade)

    /**
     * Inserta varios elementos a la base de datos
     * @param calificaciones Elementos a insertar
     */
    @Insert
    fun insertAll(calificaciones : List<CourseGrade>)

    /**
     * Elimina todos los elementos de la base de datos
     */
    @Query("DELETE FROM $GRADES_TABLE_NAME")
    fun deleteAll()

    /**
     * Elimina un elemento de la base de datos
     */
    @Delete
    fun delete(courseGrade: CourseGrade)
}