package ziox.ramiro.saes.features.saes.features.schedule.data.models

import androidx.room.*
import ziox.ramiro.saes.utils.MES
import ziox.ramiro.saes.utils.MES_COMPLETO
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
        fun fromDate(value: Date) = value.let {
            val calendar = Calendar.getInstance()
            calendar.time = value

            Hour(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        }
    }

    fun toDouble() = hours+(minutes/60.0)

    override fun toString(): String {
        return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
    }

    override fun equals(other: Any?): Boolean {
        return if(other is Hour){
            other.hours == hours && other.minutes == minutes
        }else{
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        var result = hours
        result = 31 * result + minutes
        return result
    }
}


data class ShortDate(
    val day: Int,
    val month: Int,
    val year: Int
){
    companion object{
        fun MMMddyyyy(value: String) : ShortDate{
            val values = value.split(" ")

            return ShortDate(
                values[1].toInt(),
                MES.indexOf(values[0].uppercase()),
                values[2].toInt()
            )
        }

        fun ddMMMyyyy(value: String) : ShortDate{
            val values = value.split(" ")

            return ShortDate(
                values[0].toInt(),
                MES.indexOf(values[1].uppercase()),
                values[2].toInt()
            )
        }
    }

    override fun toString(): String {
        return "$day de ${MES_COMPLETO[month]} del $year"
    }
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

