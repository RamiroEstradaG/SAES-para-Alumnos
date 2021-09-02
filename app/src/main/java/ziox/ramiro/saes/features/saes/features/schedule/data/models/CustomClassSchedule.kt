package ziox.ramiro.saes.features.saes.features.schedule.data.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_class_schedule")
data class CustomClassSchedule(
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
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
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
        parcel.readInt() != 0,
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
        parcel.writeInt(if(isDeleted) 1 else 0)
        parcel.writeParcelable(scheduleDayTime, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun toClassSchedule() = ClassSchedule(id, classId, className, group, building, classroom, teacherName, color, scheduleDayTime)

    companion object CREATOR : Parcelable.Creator<CustomClassSchedule> {
        override fun createFromParcel(parcel: Parcel): CustomClassSchedule {
            return CustomClassSchedule(parcel)
        }

        override fun newArray(size: Int): Array<CustomClassSchedule?> {
            return arrayOfNulls(size)
        }
    }
}
