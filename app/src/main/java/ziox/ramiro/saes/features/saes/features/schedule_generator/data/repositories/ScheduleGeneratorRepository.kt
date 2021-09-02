package ziox.ramiro.saes.features.saes.features.schedule_generator.data.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ziox.ramiro.saes.features.saes.features.schedule.data.models.GeneratorClassSchedule

@Dao
interface ScheduleGeneratorRepository {
    @Query("SELECT * FROM schedule_generator")
    fun fetchSchedule(): List<GeneratorClassSchedule>

    @Insert
    fun addClass(classSchedule: GeneratorClassSchedule)

    @Query("DELETE FROM schedule_generator WHERE class_id = :classId")
    fun removeClass(classId: String)
}

