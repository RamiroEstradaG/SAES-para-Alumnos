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

    private val scheduleColors = arrayOf(
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

    override suspend fun getMySchedule(): List<ClassSchedule> {
        return if(context.isNetworkAvailable()){
            webView.scrap(
                script = """
                var scheduleTable = document.getElementById("ctl00_mainCopy_PnlDatos");

                if(scheduleTable != null){
                    var schedule = scheduleTable.getElementsByTagName("tbody")[1];
                    var children = [...schedule.children];
                
                    var cols = JSON.parse(window.Utils.analiseColumns(
                        [...children[0].children].map((el) => el.innerText),
                        [...children[1].children].map((el) => el.innerText)
                    ));
                
                    children.splice(0,1);
                
                    var scheduledClass = [];
                    
                    children.filter(tr => !(!tr.innerText || /^\s*${'$'}/.test(tr.innerText))).forEach(tr => {
                        scheduledClass.push(...tr.children.map((td, e) => ({
                            id: tr.children[cols.groupIndex].innerText + tr.children[cols.subjectIndex].innerText + (e%5).toString(), 
                            dayIndex: (e - cols.mondayIndex) % 5,               
                            className: tr.children[cols.subjectIndex].innerText,               
                            hours: td.innerText,               
                            group: tr.children[cols.groupIndex].innerText,               
                            teacherName: tr.children[cols.teacherIndex].innerText,               
                            building: tr.children[cols.buildingIndex].innerText,               
                            classroom: tr.children[cols.classroomIndex].innerText
                        })));
                    });
                
                    next(scheduledClass);
                }else{
                    next([]);
                }
            """.trimIndent(),
            ){
                val data = it.result.getJSONArray("data")

                ArrayList<ClassSchedule>().apply {
                    for (i in 0 until data.length()) {
                        val classSchedule = data[i] as JSONObject

                        val hours = HourRange.parse(classSchedule.getString("hours"), WeekDay.byDayOfWeek(classSchedule.getInt("dayIndex")))

                        addAll(hours.map { range ->
                            ClassSchedule(
                                classSchedule.getString("id"),
                                classSchedule.getString("className").toProperCase(),
                                classSchedule.getString("building"),
                                classSchedule.getString("classroom"),
                                classSchedule.getString("teacherName").toProperCase(),
                                scheduleColors[i%scheduleColors.size].value.toLong(),
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