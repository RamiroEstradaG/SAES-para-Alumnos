package ziox.ramiro.saes.features.saes.features.schedule.data.repositories

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.json.JSONObject
import ziox.ramiro.saes.data.data_providers.WebViewProvider
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.HourRange
import ziox.ramiro.saes.features.saes.features.schedule.data.models.WeekDay
import ziox.ramiro.saes.features.saes.features.schedule.data.models.scheduleColors
import ziox.ramiro.saes.utils.isNetworkAvailable
import ziox.ramiro.saes.utils.runOnDefaultThread
import ziox.ramiro.saes.utils.toProperCase

interface ScheduleRepository {
    suspend fun getMySchedule() : List<ClassSchedule>
}


class ScheduleWebViewRepository(
    private val context: Context
) : ScheduleRepository {
    private val webView = WebViewProvider(context, "/Alumnos/Informacion_semestral/Horario_Alumno.aspx")
    private val persistenceRepository = LocalAppDatabase.invoke(context).scheduleRepository()

    override suspend fun getMySchedule(): List<ClassSchedule> {
        return if(context.isNetworkAvailable()){
            webView.scrap(
                script = """
                var scheduleTable = byId("ctl00_mainCopy_Panel1");

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
                    
                        
                        
                        children.filter(tr => !(!tr.innerText || /^\s*${'$'}/.test(tr.innerText))).forEach(tr => {
                            scheduledClass.push(...[...tr.children].map((td, e) => ({
                                id: tr.children[cols.groupIndex].innerText + tr.children[cols.subjectIndex].innerText + (e%5).toString(), 
                                dayIndex: (e - cols.mondayIndex) % 5 + 1,               
                                className: tr.children[cols.subjectIndex].innerText,               
                                hours: td.innerText,               
                                group: tr.children[cols.groupIndex].innerText,               
                                teacherName: tr.children[cols.teacherIndex].innerText,               
                                building: tr.children[cols.buildingIndex].innerText,               
                                classroom: tr.children[cols.classroomIndex].innerText
                            })));
                        });
                    }
                
                    next(scheduledClass);
                }else{
                    next([]);
                }
            """.trimIndent(),
            ){
                val data = it.result.getJSONArray("data")

                val registered = mutableMapOf<String, Long>()

                ArrayList<ClassSchedule>().apply {
                    for (i in 0 until data.length()) {
                        val classSchedule = data[i] as JSONObject

                        val hours = HourRange.parse(
                            classSchedule.getString("hours"),
                            WeekDay.byDayOfWeek(classSchedule.getInt("dayIndex"))
                        )

                        val className = classSchedule.getString("className").toProperCase()

                        val color = if (registered.containsKey(className)){
                            registered.getValue(className)
                        }else{
                            registered[className] = scheduleColors[registered.size % scheduleColors.size].value.toLong()
                            registered.getValue(className)
                        }

                        addAll(hours.map { range ->
                            ClassSchedule(
                                classSchedule.getString("id"),
                                className,
                                classSchedule.getString("building"),
                                classSchedule.getString("classroom"),
                                classSchedule.getString("teacherName").toProperCase(),
                                color,
                                range
                            )
                        })
                    }
                }.filter { f ->
                    f.hourRange.duration > 0
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