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
    var id: String = "",
    @ColumnInfo(name = "class_id")
    var classId: String = "",
    @ColumnInfo(name = "class_name")
    var className: String = "",
    @ColumnInfo(name = "group")
    var group: String = "",
    @ColumnInfo(name = "building")
    var building: String = "",
    @ColumnInfo(name = "classroom")
    var classroom: String = "",
    @ColumnInfo(name = "teacher_name")
    var teacherName: String = "",
    @ColumnInfo(name = "class_color")
    var color: Long = 0L,
    @ColumnInfo(name = "is_deleted")
    var isDeleted: Boolean = false,
    @Embedded
    var scheduleDayTime: ScheduleDayTime = ScheduleDayTime()
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
