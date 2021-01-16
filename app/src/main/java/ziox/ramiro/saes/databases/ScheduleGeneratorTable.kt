package ziox.ramiro.saes.databases

import androidx.room.*

const val SCHEDULE_GENERATOR_TABLE_NAME = "schedule_generator"

@Entity(tableName = SCHEDULE_GENERATOR_TABLE_NAME)
data class ScheduleGeneratorClass(
    @ColumnInfo(name = "career_name") val careerName : String,
    @ColumnInfo(name = "semester") val semester : Int,
    @ColumnInfo(name = "group") val group : String,
    @ColumnInfo(name = "course_name") val courseName : String,
    @ColumnInfo(name = "teacher_name") val teacherName : String,
    @ColumnInfo(name = "building_name") val buildingName : String,
    @ColumnInfo(name = "classroom_name") val classroomName : String,
    @ColumnInfo(name = "monday") val monday : String,
    @ColumnInfo(name = "tuesday") val tuesday : String,
    @ColumnInfo(name = "wednesday") val wednesday : String,
    @ColumnInfo(name = "thursday") val thursday : String,
    @ColumnInfo(name = "friday") val friday : String,
    @PrimaryKey(autoGenerate = true) val uid : Int = 0
)

@Dao
interface ScheduleGeneratorDao {
    @Query("SELECT * FROM $SCHEDULE_GENERATOR_TABLE_NAME")
    fun getAll() : List<ScheduleGeneratorClass>

    @Query("SELECT * FROM $SCHEDULE_GENERATOR_TABLE_NAME WHERE rowId = :rowId")
    fun get(rowId: Long) : ScheduleGeneratorClass?

    @Query("SELECT * FROM $SCHEDULE_GENERATOR_TABLE_NAME WHERE course_name = :courseName")
    fun getByCourseName(courseName: String) : List<ScheduleGeneratorClass>

    @Insert
    fun insert(generatorClass: ScheduleGeneratorClass) : Long

    @Delete
    fun delete(generatorClass: ScheduleGeneratorClass)

    @Query("DELETE FROM $SCHEDULE_GENERATOR_TABLE_NAME WHERE course_name = :courseName")
    fun deleteByCourseName(courseName: String)

    @Query("DELETE FROM $SCHEDULE_GENERATOR_TABLE_NAME WHERE uid = :uid")
    fun deleteByUid(uid: Int) : Int

    @Query("DELETE FROM $SCHEDULE_GENERATOR_TABLE_NAME")
    fun deleteAll()
}