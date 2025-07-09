package ziox.ramiro.saes.features.saes.features.schedule.data.repositories

import android.content.Context
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.json.JSONObject
import ziox.ramiro.saes.data.data_providers.WebViewProvider
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.CustomClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ScheduleDayTime
import ziox.ramiro.saes.features.saes.features.schedule.data.models.WeekDay
import ziox.ramiro.saes.features.saes.features.schedule.data.models.scheduleColors
import ziox.ramiro.saes.utils.isNetworkAvailable
import ziox.ramiro.saes.utils.runOnDefaultThread
import ziox.ramiro.saes.utils.toProperCase
import java.util.Calendar

interface ScheduleRepository {
    suspend fun getMySchedule() : List<ClassSchedule>
}


class ScheduleWebViewRepository(
    private val context: Context
) : ScheduleRepository {
    private val webView = WebViewProvider(context, "/Alumnos/Informacion_semestral/Horario_Alumno.aspx")
    private val persistenceRepository = LocalAppDatabase.invoke(context).scheduleRepository()
    private val customClassSchedule = LocalAppDatabase.invoke(context).customScheduleGeneratorRepository()

    override suspend fun getMySchedule(): List<ClassSchedule> {
        val customSchedule = runOnDefaultThread {
            customClassSchedule.getMySchedule()
        }

        return if(context.isNetworkAvailable()){
            webView.scrap(
                script = """
                try {
                    var scheduleTable = byId("ctl00_mainCopy_GV_Horario");

                    if(scheduleTable != null){
                        var schedule = scheduleTable.getElementsByTagName("tbody");
                        var scheduledClass = [];
                        
                        if(schedule.length > 0){
                            var children = [...schedule[0].children];
                        
                            var cols = JSON.parse(window.Utils.analiseColumns(
                                [...children[0].children].map((el) => el.innerText),
                                [...children[1].children].map((el) => el.innerText)
                            ));
                        
                            children.splice(0,1);
                            
                            children.filter(tr => tr.innerText.trim().length > 0).forEach((tr, trIndex) => {
                                scheduledClass.push(...[...tr.children].map((td, e) => ({
                                    classId: trIndex.toString() + tr.children[cols.subjectIndex].innerText.trim() + tr.children[cols.groupIndex].innerText.trim(),
                                    dayIndex: (e - cols.mondayIndex) % 5 + ${Calendar.MONDAY},               
                                    className: tr.children[cols.subjectIndex].innerText,               
                                    hours: td.innerText,               
                                    group: tr.children[cols.groupIndex]?.innerText ?? '',               
                                    teacherName: tr.children[cols.teacherIndex]?.innerText ?? '',               
                                    building: tr.children[cols.buildingIndex]?.innerText ?? '',               
                                    classroom: tr.children[cols.classroomIndex]?.innerText ?? ''
                                })));
                            });
                        }
                    
                        next(scheduledClass);
                    }else{
                        next([]);
                    }
                }catch(e){
                    throwError(e);
                }
            """.trimIndent(),
            ){
                val data = it.result.getJSONArray("data")
                val registered = mutableMapOf<String, Long>()

                ArrayList<ClassSchedule?>().apply {
                    for (i in 0 until data.length()) {
                        val classSchedule = data[i] as JSONObject

                        val hours = ScheduleDayTime.parse(
                            classSchedule.getString("hours"),
                            WeekDay.byDayOfWeek(classSchedule.getInt("dayIndex"))
                        )

                        val classId = classSchedule.getString("classId")

                        val color = if (registered.containsKey(classId)){
                            registered.getValue(classId)
                        }else{
                            registered[classId] = scheduleColors[registered.size % scheduleColors.size].value.toLong()
                            registered.getValue(classId)
                        }

                        addAll(hours.mapIndexed { rangeIndex, range ->
                            val group = classSchedule.getString("group")
                            val className = classSchedule.getString("className").toProperCase()

                            val id = "${classId}_${group}_${className.replace(" ", "_")}_${range.weekDay}_${range.start.toDouble()}_${rangeIndex}"

                            val customClass = customSchedule.find { cc -> cc.id == id }

                            if (customClass?.isDeleted == true) return@mapIndexed null

                            customClass?.toClassSchedule() ?: ClassSchedule(
                                id,
                                classId,
                                className,
                                group,
                                classSchedule.getString("building"),
                                classSchedule.getString("classroom"),
                                classSchedule.getString("teacherName").toProperCase(),
                                color,
                                range
                            )
                        })
                    }
                }.filterNotNull().filter { f ->
                    f.scheduleDayTime.duration > 0
                }
            }.also {
                runOnDefaultThread {
                    persistenceRepository.removeSchedule()
                    persistenceRepository.addSchedule(it)
                }
            }
        }else{
            runOnDefaultThread {
                persistenceRepository.getMySchedule()
            }
        }
    }
}

@Dao
interface ScheduleRoomRepository{
    @Query("SELECT * FROM class_schedule")
    fun getMySchedule() : List<ClassSchedule>

    @Insert
    fun addSchedule(schedule: List<ClassSchedule>)

    @Query("DELETE FROM class_schedule")
    fun removeSchedule()
}

@Dao
interface CustomScheduleRoomRepository{
    @Query("SELECT * FROM custom_class_schedule")
    fun getMySchedule() : List<CustomClassSchedule>

    @Insert
    fun addSchedule(schedule: List<CustomClassSchedule>)

    @Insert
    fun addClass(customClassSchedule: CustomClassSchedule)

    @Query("DELETE FROM custom_class_schedule")
    fun removeSchedule()

    @Query("UPDATE custom_class_schedule SET is_deleted = 1 WHERE id = :id")
    fun hideClass(id: String)

    @Query("DELETE FROM custom_class_schedule WHERE id = :id")
    fun removeClass(id: String)
}