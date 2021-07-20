package ziox.ramiro.saes.features.saes.features.schedule.data.models

import androidx.compose.ui.graphics.Color
import androidx.room.*
import ziox.ramiro.saes.utils.MES
import ziox.ramiro.saes.utils.MES_COMPLETO
import ziox.ramiro.saes.utils.toCalendar
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
    val hourRange: HourRange
)

@Entity(tableName = "schedule_generator")
data class GeneratorClassSchedule(
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
    val hourRange: HourRange
)

val scheduleColors = arrayOf(
    Color(0xFFE91E63),
    Color(0xFFFF9800),
    Color(0xFF9C27B0),
    Color(0xFF4CAF50),
    Color(0xFF2196F3),
    Color(0xFFF44336),
    Color(0xFF673AB7),
    Color(0xFF8BC34A),
    Color(0xFF03A9F4),
    Color(0xFFFF5722),
    Color(0xFFFFC107),
    Color(0xFF009688),
    Color(0xFF3F51B5),
    Color(0xFFCDDC39)
)

fun List<ClassSchedule>.getCurrentClass() : ClassSchedule? {
    val currentDay = WeekDay.today()
    val currentHour = Hour.fromDate(Date())

    return this.find {
        currentDay == it.hourRange.weekDay && currentHour.toDouble() in it.hourRange.start.toDouble()..it.hourRange.end.toDouble()
    }
}

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
        fun parse(hourRange: String, weekDay: WeekDay = WeekDay.UNKNOWN): List<HourRange>{
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

fun <T>List<T>.getRangeBy(block: (T) -> HourRange) : IntRange{
    var start : Double? = null
    var end : Double? = null

    for (item in this) {
        val hourRange = block(item)
        if(start?.compareTo(hourRange.start.toDouble()) ?: 1 > 0){
            start = hourRange.start.toDouble()
        }

        if(end?.compareTo(hourRange.end.toDouble()) ?: -1 < 0){
            end = hourRange.end.toDouble()
        }
    }

    val first = start?.toInt() ?: 0
    val last = end?.toInt() ?: 0

    val diff = last - first

    return IntRange(first, last + if(diff < 5) 5 - diff else 1)
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

        fun fromDate(date: Date) : ShortDate {
            val calendar =  date.toCalendar()

            return ShortDate(
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.YEAR)
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

    fun toDate(): Date = Calendar.getInstance().apply {
        timeInMillis = 0L
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, day)
    }.time

    override fun toString(): String {
        return "$day de ${MES_COMPLETO[month]} del $year"
    }

    override fun equals(other: Any?): Boolean {
        return if(other is ShortDate){
            other.day == day
                && other.month == month
                && other.year == year
        }else{
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        var result = day
        result = 31 * result + month
        result = 31 * result + year
        return result
    }
}

enum class WeekDay(val dayName: String, val calendarDayIndex: Int){
    MONDAY("Lunes", 1),
    TUESDAY("Martes", 2),
    WEDNESDAY("Miércoles", 3),
    THURSDAY("Jueves", 4),
    FRIDAY("Viernes", 5),
    UNKNOWN("Desconocido", -1);

    companion object {
        fun today() = byDate(Date())

        fun byDate(date: Date) = byDayOfWeek(date.toCalendar().get(Calendar.DAY_OF_WEEK))

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

