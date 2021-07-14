package ziox.ramiro.saes.features.saes.features.schedule.data.models

import androidx.room.*
import java.util.*

@Entity(tableName = "class_schedule")
data class ClassSchedule(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "class_name")
    val className: String,
    @ColumnInfo(name = "building")
    val building: String,
    @ColumnInfo(name = "classroom")
    val classroom: String,
    @ColumnInfo(name = "teacher_name")
    val teacherName: String,
    @ColumnInfo(name = "class_color")
    val color: Long,
    @Embedded
    val hour: HourRange
)

data class HourRange(
    @ColumnInfo(name = "hour_start")
    val start: Hour,
    @ColumnInfo(name = "hour_end")
    val end: Hour,
    @ColumnInfo(name = "weekday")
    val weekDay: WeekDay
){
    @Ignore
    val duration: Double = end.toDouble() - start.toDouble()

    companion object {
        fun parse(hourRange: String, weekDay: WeekDay): List<HourRange>{
            val hours = Regex("[0-9]+:[0-9]+-[0-9]+:[0-9]+").findAll(hourRange.replace(" ", ""))

            return hours.map {
                val values = it.value.split("-")
                HourRange(
                    Hour.parse(values[0])!!,
                    Hour.parse(values[1])!!,
                    weekDay
                )
            }.toList()
        }
    }
}

data class Hour(
    val hours: Int,
    val minutes: Int
){
    companion object {
        fun parse(hour: String): Hour?{
            val hourFind = Regex("[0-9]+:[0-9]+").find(hour.replace(" ", ""))?.value?.split(":")

            return if (hourFind?.size == 2){
                Hour(hourFind[0].toInt(), hourFind[1].toInt())
            }else null
        }

        fun fromValue(value: Double) = Hour(value.toInt(), value.mod(1.0).times(60).toInt())
    }

    fun toDouble() = hours+(minutes/60.0)
}

enum class WeekDay(val dayName: String){
    MONDAY("Lunes"),
    TUESDAY("Martes"),
    WEDNESDAY("Miércoles"),
    THURSDAY("Jueves"),
    FRIDAY("Viernes"),
    UNKNOWN("Desconocido");

    companion object {
        fun todayByCalendar() = byDayOfWeek(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))

        fun byDayOfWeek(dayOfWeek : Int) = when(dayOfWeek){
            1 -> MONDAY
            2 -> TUESDAY
            3 -> WEDNESDAY
            4 -> THURSDAY
            5 -> FRIDAY
            else -> UNKNOWN
        }
    }
}

