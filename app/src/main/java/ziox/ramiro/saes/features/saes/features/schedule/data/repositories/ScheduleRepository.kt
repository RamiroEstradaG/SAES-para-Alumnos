package ziox.ramiro.saes.features.saes.features.schedule.data.repositories

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.json.JSONObject
import ziox.ramiro.saes.data.data_provider.createWebView
import ziox.ramiro.saes.data.data_provider.scrap
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.utils.toProperCase

interface ScheduleRepository {
    suspend fun getMySchedule() : List<ClassSchedule>
}


class ScheduleWebViewRepository(
    context: Context
) : ScheduleRepository {
    private val webView = createWebView(context)

    override suspend fun getMySchedule(): List<ClassSchedule> {
        return webView.scrap(
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
            path = "/Alumnos/Informacion_semestral/Horario_Alumno.aspx"
        ){
            val data = it.result.getJSONArray("data")

            List(data.length()){ i ->
                val classSchedule = data[i] as JSONObject

                ClassSchedule(
                    classSchedule.getString("id"),
                    classSchedule.getString("className").toProperCase(),
                    classSchedule.getString("building"),
                    classSchedule.getString("classroom"),
                    classSchedule.getString("teacherName").toProperCase(),
                    Color.Red,
                    ClassSchedule.Hour(classSchedule.getString("hours"), ClassSchedule.WeekDay.byDayOfWeek(classSchedule.getInt("dayIndex")))
                )
            }.filter { f ->
                f.hour.duration > 0
            }
        }
    }
}