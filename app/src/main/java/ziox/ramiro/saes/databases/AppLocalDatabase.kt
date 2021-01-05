package ziox.ramiro.saes.databases

import android.content.Context
import androidx.room.*

@Database(entities = [
    KardexClass::class,
    CourseStatus::class,
    ScheduleClass::class,
    AdjustedScheduleClass::class,
    OriginalScheduleClass::class,
    PersonalScheduleClass::class,
    CourseGrade::class,
    AgendaEvent::class,
    ReEnrollmentData::class,
    ScheduleGeneratorClass::class
], version = 7, exportSchema = false)
abstract class AppLocalDatabase : RoomDatabase() {
    abstract fun kardexDao() : KardexDao
    abstract fun overallStatusDao() : OverallStatusDao
    abstract fun originalClassScheduleDao() : OriginalClassScheduleDao
    abstract fun adjustedClassScheduleDao() : AdjustedClassScheduleDao
    abstract fun personalClassScheduleDao() : PersonalClassScheduleDao
    abstract fun gradesDao() : GradesDao
    abstract fun agendaDao() : AgendaDao
    abstract fun reEnrollmentDao() : ReEnrollmentDao
    abstract fun scheduleGeneratorDao() : ScheduleGeneratorDao


    companion object {
        private const val DATABASE_NAME = "saes_local_database"
        @Volatile
        private lateinit var appLocalDatabase: AppLocalDatabase

        fun getInstance(context: Context): AppLocalDatabase {
            if (!::appLocalDatabase.isInitialized){
                synchronized(this) {
                    appLocalDatabase = Room.databaseBuilder(
                        context.applicationContext,
                        AppLocalDatabase::class.java,
                        DATABASE_NAME
                    ).enableMultiInstanceInvalidation()
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                }
            }
            return appLocalDatabase
        }
    }
}