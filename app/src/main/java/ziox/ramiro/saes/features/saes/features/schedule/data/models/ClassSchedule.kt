package ziox.ramiro.saes.features.saes.features.schedule.data.models

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import androidx.room.*
import ziox.ramiro.saes.utils.MES
import ziox.ramiro.saes.utils.MES_COMPLETO
import ziox.ramiro.saes.utils.toCalendar
import java.util.*

@Entity(tableName = "class_schedule")
data class ClassSchedule(
    @PrimaryKey
    val id: String = "",
    @ColumnInfo(name = "class_id")
    val classId: String = "",
    @ColumnInfo(name = "class_name")
    val className: String = "",
    @ColumnInfo(name = "group")
    val group: String = "",
    @ColumnInfo(name = "building")
    val building: String = "",
    @ColumnInfo(name = "classroom")
    val classroom: String = "",
    @ColumnInfo(name = "teacher_name")
    val teacherName: String = "",
    @ColumnInfo(name = "class_color")
    val color: Long = 0L,
    @Embedded
    val scheduleDayTime: ScheduleDayTime = ScheduleDayTime()
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readParcelable(ScheduleDayTime::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(classId)
        parcel.writeString(className)
        parcel.writeString(group)
        parcel.writeString(building)
        parcel.writeString(classroom)
        parcel.writeString(teacherName)
        parcel.writeLong(color)
        parcel.writeParcelable(scheduleDayTime, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ClassSchedule> {
        override fun createFromParcel(parcel: Parcel): ClassSchedule {
            return ClassSchedule(parcel)
        }

        override fun newArray(size: Int): Array<ClassSchedule?> {
            return arrayOfNulls(size)
        }

        fun fromGeneratorClassSchedule(classSchedule: GeneratorClassSchedule) = ClassSchedule(
            classSchedule.id,
            classSchedule.classId,
            classSchedule.className,
            classSchedule.group,
            classSchedule.building,
            classSchedule.classroom,
            classSchedule.teacherName,
            classSchedule.color,
            classSchedule.scheduleDayTime
        )
    }
}

fun checkIfOccupied(list: List<ScheduleDayTime>, item: ScheduleDayTime): Int?{
    for ((i, classSchedule) in list.withIndex()) {
        if(item.weekDay == classSchedule.weekDay && (item.start.toDouble() in classSchedule.start.toDouble()..(classSchedule.end.toDouble() - 0.01)
            || item.end.toDouble() in (classSchedule.start.toDouble() + 0.01)..classSchedule.end.toDouble()
            || classSchedule.start.toDouble() in item.start.toDouble()..(item.end.toDouble() - 0.01)
            || classSchedule.end.toDouble() in (item.start.toDouble() + 0.01)..item.end.toDouble())){
            return i
        }
    }

    return null
}

data class ClassScheduleCollection(
    val className: String,
    val group: String,
    val teacherName: String,
    val classId: String,
    val schedules: List<ClassSchedule>
): Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createTypedArrayList(ClassSchedule)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(className)
        parcel.writeString(group)
        parcel.writeString(teacherName)
        parcel.writeString(classId)
        parcel.writeTypedList(schedules)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ClassScheduleCollection> {
        override fun createFromParcel(parcel: Parcel): ClassScheduleCollection {
            return ClassScheduleCollection(parcel)
        }

        override fun newArray(size: Int): Array<ClassScheduleCollection?> {
            return arrayOfNulls(size)
        }

        fun fromClassScheduleList(classSchedules: List<ClassSchedule>): List<ClassScheduleCollection>{
            val groups = classSchedules.groupBy {
                it.classId
            }

            return groups.map {
                val classSchedule = it.value.first()
                ClassScheduleCollection(
                    classSchedule.className,
                    classSchedule.group,
                    classSchedule.teacherName,
                    classSchedule.classId,
                    it.value
                )
            }
        }

        fun toClassScheduleList(classCollection: List<ClassScheduleCollection>): List<ClassSchedule>{
            return ArrayList<ClassSchedule>().apply {
                classCollection.forEach {
                    addAll(it.schedules)
                }
            }
        }

        fun toGeneratorClassScheduleList(classCollection: ClassScheduleCollection): List<GeneratorClassSchedule>{
            return classCollection.schedules.map {
                GeneratorClassSchedule.fromClassSchedule(it)
            }
        }
    }


}

@Entity(tableName = "schedule_generator")
data class GeneratorClassSchedule(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "class_id")
    val classId: String,
    @ColumnInfo(name = "class_name")
    val className: String,
    @ColumnInfo(name = "group")
    val group: String,
    @ColumnInfo(name = "building")
    val building: String,
    @ColumnInfo(name = "classroom")
    val classroom: String,
    @ColumnInfo(name = "teacher_name")
    val teacherName: String,
    @ColumnInfo(name = "class_color")
    val color: Long,
    @Embedded
    val scheduleDayTime: ScheduleDayTime
) : Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readParcelable(ScheduleDayTime::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(classId)
        parcel.writeString(className)
        parcel.writeString(group)
        parcel.writeString(building)
        parcel.writeString(classroom)
        parcel.writeString(teacherName)
        parcel.writeLong(color)
        parcel.writeParcelable(scheduleDayTime, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GeneratorClassSchedule> {
        override fun createFromParcel(parcel: Parcel): GeneratorClassSchedule {
            return GeneratorClassSchedule(parcel)
        }

        override fun newArray(size: Int): Array<GeneratorClassSchedule?> {
            return arrayOfNulls(size)
        }

        fun fromClassSchedule(classSchedule: ClassSchedule) = GeneratorClassSchedule(
            classSchedule.id,
            classSchedule.classId,
            classSchedule.className,
            classSchedule.group,
            classSchedule.building,
            classSchedule.classroom,
            classSchedule.teacherName,
            classSchedule.color,
            classSchedule.scheduleDayTime
        )
    }

}

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

    return this.filter {
        currentDay == it.scheduleDayTime.weekDay
    }.find {
         currentHour.toDouble() in it.scheduleDayTime.start.toDouble()..it.scheduleDayTime.end.toDouble()
    }
}

fun List<ClassSchedule>.getNextClass() : ClassSchedule? {
    val currentDay = WeekDay.today()
    val currentHour = Hour.fromDate(Date())
    var currentMinHour = 24.0
    var currentResult: ClassSchedule? = null

    val list = this.filter {
        currentDay == it.scheduleDayTime.weekDay
    }.sortedByDescending {
        it.scheduleDayTime.start.toDouble()
    }

    list.forEach {
        if(it.scheduleDayTime.start.toDouble() < currentMinHour && it.scheduleDayTime.start.toDouble() >= currentHour.toDouble()){
            currentMinHour = it.scheduleDayTime.start.toDouble()
            currentResult = it
        }
    }

    return currentResult
}

data class ScheduleDayTime(
    @ColumnInfo(name = "hour_start")
    val start: Hour = Hour(),
    @ColumnInfo(name = "hour_end")
    val end: Hour = Hour(),
    @ColumnInfo(name = "weekday")
    val weekDay: WeekDay = WeekDay.UNKNOWN
): Parcelable{
    @Ignore
    val duration: Double = end.toDouble() - start.toDouble()

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Hour::class.java.classLoader)!!,
        parcel.readParcelable(Hour::class.java.classLoader)!!,
        WeekDay.valueOf(parcel.readString()!!)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(start, flags)
        parcel.writeParcelable(end, flags)
        parcel.writeString(weekDay.name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ScheduleDayTime> {
        fun parse(hourRange: String, weekDay: WeekDay = WeekDay.UNKNOWN): List<ScheduleDayTime>{
            val hours = Regex("[0-9]+:[0-9]+\\s*-\\s*[0-9]+:[0-9]+").findAll(hourRange)

            return hours.map {
                val values = it.value.replace(" ", "").split("-")
                ScheduleDayTime(
                    Hour.parse(values[0])!!,
                    Hour.parse(values[1])!!,
                    weekDay
                )
            }.toList()
        }

        override fun createFromParcel(parcel: Parcel): ScheduleDayTime {
            return ScheduleDayTime(parcel)
        }

        override fun newArray(size: Int): Array<ScheduleDayTime?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "$start - $end"
    }
}

fun <T>List<T>.getRangeBy(block: (T) -> ScheduleDayTime) : IntRange{
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
    val hours: Int = 0,
    val minutes: Int = 0
): Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt()
    )

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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(hours)
        parcel.writeInt(minutes)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Hour> {
        fun parse(hour: String): Hour?{
            val hourFind = Regex("[0-9]+:[0-9]+").find(hour.replace(" ", ""))?.value?.split(":")

            return if (hourFind?.size == 2){
                Hour(hourFind[0].toInt(), hourFind[1].toInt())
            }else null
        }

        fun now() = fromDate(Date())
        fun fromValue(value: Double) = Hour(value.toInt(), value.mod(1.0).times(60).toInt())
        fun fromDate(value: Date) = value.let {
            val calendar = Calendar.getInstance(TimeZone.getDefault())
            calendar.time = value

            Hour(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        }

        override fun createFromParcel(parcel: Parcel): Hour {
            return Hour(parcel)
        }

        override fun newArray(size: Int): Array<Hour?> {
            return arrayOfNulls(size)
        }
    }
}


data class ShortDate(
    val day: Int = 0,
    val month: Int = 0,
    val year: Int = 0
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

    fun toDate(): Date = Calendar.getInstance(TimeZone.getDefault()).apply {
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

        fun byDate(date: Date)
            = byDayOfWeek(date.toCalendar().get(Calendar.DAY_OF_WEEK))

        fun byDayOfWeek(dayOfWeek : Int) = when(dayOfWeek){
            Calendar.MONDAY -> MONDAY
            Calendar.TUESDAY -> TUESDAY
            Calendar.WEDNESDAY -> WEDNESDAY
            Calendar.THURSDAY -> THURSDAY
            Calendar.FRIDAY -> FRIDAY
            else -> UNKNOWN
        }
    }
}

