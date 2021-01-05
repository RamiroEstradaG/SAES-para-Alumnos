package ziox.ramiro.saes.databases

import androidx.room.*

/**
 * Nombre de la tabla que contiene el horario original
 */
const val ORIGINAL_SCHEDULE_CLASS_TABLE_NAME = "original_schedule_class"
/**
 * Nombre de la tabla que contiene clases extracurriculares
 */
const val PERSONAL_SCHEDULE_CLASS_TABLE_NAME = "personal_schedule_class"
/**
 * Nombre de la tabla que contiene las correcciones de las clases de [ORIGINAL_SCHEDULE_CLASS_TABLE_NAME]
 */
const val ADJUSTED_SCHEDULE_CLASS_TABLE_NAME = "adjusted_schedule_class"

@Entity
open class ScheduleClass(
    @PrimaryKey var uid: String,
    @ColumnInfo(name = "day_index") var dayIndex: Int,
    @ColumnInfo(name = "course_name") var courseName: String,
    @ColumnInfo(name = "start_hour") var startHour: Double,
    @ColumnInfo(name = "finish_hour") var finishHour: Double,
    @ColumnInfo(name = "color") var color: String,
    @ColumnInfo(name = "group") var group: String,
    @ColumnInfo(name = "teacher_name") var teacherName : String,
    @ColumnInfo(name = "building_name") var buildingName : String,
    @ColumnInfo(name = "classroom_name") var classroomName: String,
    @ColumnInfo(name = "is_user_personal_class") var isUserPersonalClass : Boolean
){
    fun copy() = ScheduleClass(
        uid, dayIndex, courseName, startHour, finishHour, color, group, teacherName, buildingName, classroomName, isUserPersonalClass
    )

    override fun toString(): String {
        return """
            uid = $uid,
            dayIndex = $dayIndex
            courseName = $courseName
            startHour = $startHour
            finishHour = $finishHour
            color = $color
            group = $group
            teacherName = $teacherName
            buildingName = $buildingName
            classroomName = $classroomName
            isUserPersonalClass = $isUserPersonalClass
        """.trimIndent()
    }

    fun toOriginalScheduleClass() : OriginalScheduleClass = OriginalScheduleClass(
        uid, dayIndex, courseName, startHour, finishHour, color, group, teacherName, buildingName, classroomName, isUserPersonalClass
    )

    fun toAdjustedScheduleClass() : AdjustedScheduleClass = AdjustedScheduleClass(
        uid, dayIndex, courseName, startHour, finishHour, color, group, teacherName, buildingName, classroomName, isUserPersonalClass
    )

    fun toPersonalScheduleClass() : PersonalScheduleClass = PersonalScheduleClass(
        uid, dayIndex, courseName, startHour, finishHour, color, group, teacherName, buildingName, classroomName, isUserPersonalClass
    )

    fun toScheduleClass() : ScheduleClass = ScheduleClass(
        uid, dayIndex, courseName, startHour, finishHour, color, group, teacherName, buildingName, classroomName, isUserPersonalClass
    )
}

@Entity(tableName = ORIGINAL_SCHEDULE_CLASS_TABLE_NAME)
class OriginalScheduleClass(
    uid: String,
    dayIndex: Int,
    courseName: String,
    startHour: Double,
    finishHour: Double,
    color: String,
    group: String,
    teacherName : String,
    buildingName : String,
    classroomName: String,
    isUserPersonalClass : Boolean
) : ScheduleClass(
    uid, dayIndex, courseName, startHour, finishHour, color, group, teacherName, buildingName, classroomName, isUserPersonalClass
)

@Entity(tableName = ADJUSTED_SCHEDULE_CLASS_TABLE_NAME)
class AdjustedScheduleClass(
    uid: String,
    dayIndex: Int,
    courseName: String,
    startHour: Double,
    finishHour: Double,
    color: String,
    group: String,
    teacherName : String,
    buildingName : String,
    classroomName: String,
    isUserPersonalClass : Boolean
) : ScheduleClass(
    uid, dayIndex, courseName, startHour, finishHour, color, group, teacherName, buildingName, classroomName, isUserPersonalClass
)

@Entity(tableName = PERSONAL_SCHEDULE_CLASS_TABLE_NAME)
class PersonalScheduleClass(
    uid: String,
    dayIndex: Int,
    courseName: String,
    startHour: Double,
    finishHour: Double,
    color: String,
    group: String,
    teacherName : String,
    buildingName : String,
    classroomName: String,
    isUserPersonalClass : Boolean
) : ScheduleClass(
    uid, dayIndex, courseName, startHour, finishHour, color, group, teacherName, buildingName, classroomName, isUserPersonalClass
)

/**
 * DAO del Horario
 */
@Dao
interface OriginalClassScheduleDao{
    /**
     * Obtiene todas las clases de la base de datos
     */
    @Query("SELECT * FROM $ORIGINAL_SCHEDULE_CLASS_TABLE_NAME")
    fun getAll() : List<OriginalScheduleClass>

    /**
     * Inserta un elemento a la base de datos
     * @param scheduleClass la clase a insertar
     */
    @Insert
    fun insert(scheduleClass : OriginalScheduleClass) : Long

    /**
     * Revisa si alguna clase se encuentra dentro de la base de datos
     * @param uid el UID de la clase que se quiere revisar existencia
     */
    @Query("SELECT EXISTS(SELECT * FROM $ORIGINAL_SCHEDULE_CLASS_TABLE_NAME WHERE uid = :uid)")
    fun contains(uid : String) : Boolean

    /**
     * Obtiene un elemento de la base de datos
     * @param uid el UID de la clase que se quiere onbtener
     */
    @Query("SELECT * FROM $ORIGINAL_SCHEDULE_CLASS_TABLE_NAME WHERE uid = :uid LIMIT 1")
    fun get(uid : String) : OriginalScheduleClass?

    /**
     * Inserta N elementos a la base de datos
     * @param items Las clases a insertar
     */
    @Insert
    fun insertAll(items : List<OriginalScheduleClass>) : List<Long>

    /**
     * Elimina todos los elementos de la base de datos
     */
    @Query("DELETE FROM $ORIGINAL_SCHEDULE_CLASS_TABLE_NAME")
    fun deleteAll()

    /**
     * Elimina una clase de la base de datos
     * @param scheduleClass la clase a eliminar
     */
    @Delete
    fun delete(scheduleClass: OriginalScheduleClass)
}

/**
 * DAO del Horario Original con conrrecciones
 */
@Dao
interface AdjustedClassScheduleDao{
    /**
     * Obtiene todas las clases de la base de datos
     */
    @Query("SELECT * FROM $ADJUSTED_SCHEDULE_CLASS_TABLE_NAME")
    fun getAll() : List<AdjustedScheduleClass>

    /**
     * Inserta un elemento a la base de datos
     * @param clase la clase a insertar
     */
    @Insert
    fun insert(clase : AdjustedScheduleClass)

    /**
     * Revisa si alguna clase se encuentra dentro de la base de datos
     * @param claseUid el UID de la clase que se quiere revisar existencia
     */
    @Query("SELECT EXISTS(SELECT * FROM $ADJUSTED_SCHEDULE_CLASS_TABLE_NAME WHERE uid = :claseUid)")
    fun contains(claseUid : String) : Boolean

    /**
     * Obtiene un elemento de la base de datos
     * @param claseUid el UID de la clase que se quiere onbtener
     */
    @Query("SELECT * FROM $ADJUSTED_SCHEDULE_CLASS_TABLE_NAME WHERE uid = :claseUid LIMIT 1")
    fun get(claseUid : String) : AdjustedScheduleClass?

    /**
     * Inserta N elementos a la base de datos
     * @param clases Las clases a insertar
     */
    @Insert
    fun insertAll(vararg clases : AdjustedScheduleClass)

    /**
     * Elimina todos los elementos de la base de datos
     */
    @Query("DELETE FROM $ADJUSTED_SCHEDULE_CLASS_TABLE_NAME")
    fun deleteAll()

    /**
     * Elimina una clase de la base de datos
     * @param horarioData la clase a eliminar
     */
    @Delete
    fun delete(horarioData: AdjustedScheduleClass)
}

/**
 * DAO del Horario con clases personalizadas por el usuario
 */
@Dao
interface PersonalClassScheduleDao{
    /**
     * Obtiene todas las clases de la base de datos
     */
    @Query("SELECT * FROM $PERSONAL_SCHEDULE_CLASS_TABLE_NAME")
    fun getAll() : List<PersonalScheduleClass>

    /**
     * Inserta un elemento a la base de datos
     * @param item la clase a insertar
     */
    @Insert
    fun insert(item : PersonalScheduleClass)

    /**
     * Revisa si alguna clase se encuentra dentro de la base de datos
     * @param uid el UID de la clase que se quiere revisar existencia
     */
    @Query("SELECT EXISTS(SELECT * FROM $PERSONAL_SCHEDULE_CLASS_TABLE_NAME WHERE uid = :uid)")
    fun contains(uid : String) : Boolean

    /**
     * Obtiene un elemento de la base de datos
     * @param uid el UID de la clase que se quiere onbtener
     */
    @Query("SELECT * FROM $PERSONAL_SCHEDULE_CLASS_TABLE_NAME WHERE uid = :uid LIMIT 1")
    fun get(uid : String) : PersonalScheduleClass?

    /**
     * Inserta N elementos a la base de datos
     * @param items Las clases a insertar
     */
    @Insert
    fun insertAll(vararg items : PersonalScheduleClass)

    /**
     * Elimina todos los elementos de la base de datos
     */
    @Query("DELETE FROM $PERSONAL_SCHEDULE_CLASS_TABLE_NAME")
    fun deleteAll()

    /**
     * Elimina una clase de la base de datos
     * @param scheduleClass la clase a eliminar
     */
    @Delete
    fun delete(scheduleClass: PersonalScheduleClass)
}