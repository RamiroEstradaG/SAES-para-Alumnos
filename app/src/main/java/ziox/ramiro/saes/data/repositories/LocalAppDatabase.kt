package ziox.ramiro.saes.data.repositories

import android.content.Context
import androidx.room.*
import okhttp3.Headers
import org.json.JSONObject
import ziox.ramiro.saes.features.saes.data.models.HistoryItem
import ziox.ramiro.saes.features.saes.data.repositories.HistoryRoomRepository
import ziox.ramiro.saes.features.saes.features.ets.data.models.ETS
import ziox.ramiro.saes.features.saes.features.ets.data.models.ETSScore
import ziox.ramiro.saes.features.saes.features.ets.data.repositories.ETSRoomRepository
import ziox.ramiro.saes.features.saes.features.grades.data.models.ClassGrades
import ziox.ramiro.saes.features.saes.features.grades.data.repositories.GradesRoomRepository
import ziox.ramiro.saes.features.saes.features.kardex.data.models.KardexDataRoom
import ziox.ramiro.saes.features.saes.features.kardex.data.repositories.KardexRoomRepository
import ziox.ramiro.saes.features.saes.features.profile.data.models.ProfileUser
import ziox.ramiro.saes.features.saes.features.profile.data.repositories.ProfileRoomRepository
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.GeneratorClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.Hour
import ziox.ramiro.saes.features.saes.features.schedule.data.repositories.ScheduleRoomRepository
import ziox.ramiro.saes.features.saes.features.schedule_generator.models.reposotories.ScheduleGeneratorRepository
import java.util.*

@Database(entities = [
    ETS::class,
    ETSScore::class,
    ProfileUser::class,
    ClassGrades::class,
    ClassSchedule::class,
    KardexDataRoom::class,
    HistoryItem::class,
    GeneratorClassSchedule::class
], version = 9, exportSchema = false)
@TypeConverters(Converters::class)
abstract class LocalAppDatabase : RoomDatabase() {
    companion object {
        @Volatile private var instance: LocalAppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context)= instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it}
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
            LocalAppDatabase::class.java, "local-app-database")
            .fallbackToDestructiveMigration()
            .build()
    }

    abstract fun etsRepository(): ETSRoomRepository
    abstract fun userRepository(): ProfileRoomRepository
    abstract fun gradesRepository(): GradesRoomRepository
    abstract fun scheduleRepository(): ScheduleRoomRepository
    abstract fun kardexRepository(): KardexRoomRepository
    abstract fun historyRepository(): HistoryRoomRepository
    abstract fun scheduleGeneratorRepository(): ScheduleGeneratorRepository
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long) = Date(value)

    @TypeConverter
    fun dateToTimestamp(value: Date) = value.time

    @TypeConverter
    fun headersToString(value: Headers) = value.get("Cookie")

    @TypeConverter
    fun stringToHeaders(value: String): Headers = Headers.Builder().add("Cookie", value).build()

    @TypeConverter
    fun jsonToString(value: JSONObject): String = value.toString()

    @TypeConverter
    fun stringToJson(value: String): JSONObject = JSONObject(value)

    @TypeConverter
    fun hourToDouble(hour: Hour) = hour.toDouble()

    @TypeConverter
    fun doubleToHour(value: Double) = Hour.fromValue(value)
}