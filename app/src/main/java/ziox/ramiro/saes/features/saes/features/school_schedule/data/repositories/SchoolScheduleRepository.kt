package ziox.ramiro.saes.features.saes.features.school_schedule.data.repositories

import android.content.Context
import org.json.JSONObject
import ziox.ramiro.saes.data.data_providers.FilterType
import ziox.ramiro.saes.data.data_providers.WebViewProvider
import ziox.ramiro.saes.features.saes.data.models.FilterField
import ziox.ramiro.saes.features.saes.data.models.FilterRepository
import ziox.ramiro.saes.features.saes.data.models.RadioGroupFilterField
import ziox.ramiro.saes.features.saes.data.models.SelectFilterField
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ScheduleDayTime
import ziox.ramiro.saes.features.saes.features.schedule.data.models.WeekDay
import ziox.ramiro.saes.features.saes.features.schedule.data.models.scheduleColors
import ziox.ramiro.saes.utils.toProperCase
import java.util.*

interface SchoolScheduleRepository: FilterRepository {
    suspend fun getSchoolSchedule(): List<ClassSchedule>
}

class SchoolScheduleWebViewRepository(
    context: Context
): SchoolScheduleRepository {
    private val webViewProvider = WebViewProvider(context, "/Academica/horarios.aspx")

    override suspend fun getSchoolSchedule(): List<ClassSchedule> {
        return webViewProvider.scrap(
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
                        
                        children.filter(tr => tr.innerText.trim().length > 0).forEach((tr, trIndex) => {
                            scheduledClass.push(...[...tr.children].map((td, e) => ({
                                id: trIndex.toString() + tr.children[cols.groupIndex].innerText + tr.children[cols.subjectIndex].innerText + (e%5).toString(),
                                classId: trIndex.toString() + tr.children[cols.subjectIndex].innerText.trim() + tr.children[cols.groupIndex].innerText.trim(),
                                dayIndex: (e - cols.mondayIndex) % 5 + ${Calendar.MONDAY},               
                                className: tr.children[cols.subjectIndex].innerText.trim(),               
                                hours: td.innerText.trim(),               
                                group: tr.children[cols.groupIndex].innerText.trim(),               
                                teacherName: tr.children[cols.teacherIndex].innerText.trim(),               
                                building: tr.children[cols.buildingIndex].innerText.trim(),               
                                classroom: tr.children[cols.classroomIndex].innerText.trim()
                            })));
                        });
                    }
                
                    next(scheduledClass);
                }else{
                    next([]);
                }
            """.trimIndent(),
            reloadPage = false
        ) {
            val data = it.result.getJSONArray("data")

            val registered = mutableMapOf<String, Long>()

            ArrayList<ClassSchedule>().apply {
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

                    addAll(hours.map { range ->
                        ClassSchedule(
                            classSchedule.getString("id")+range.start.toString(),
                            classId,
                            classSchedule.getString("className").toProperCase(),
                            classSchedule.getString("group"),
                            classSchedule.getString("building"),
                            classSchedule.getString("classroom"),
                            classSchedule.getString("teacherName").toProperCase(),
                            color,
                            range
                        )
                    })
                }
            }.filter { f ->
                f.scheduleDayTime.duration > 0
            }
        }
    }

    override suspend fun getFilters(): List<FilterField> {
        return webViewProvider.runThenScrap(
            preRequest = "byId(\"ctl00_mainCopy_cmdVisalizar\").click();",
            postRequest = """
                next([
                    selectToFilterField("ctl00_mainCopy_Filtro_cboCarrera", "Carrera", 0),
                    selectToFilterField("ctl00_mainCopy_Filtro_cboPlanEstud", "Plan de estudios", 0),
                    selectToFilterField("ctl00_mainCopy_Filtro_cboTurno", "Turno", 0),
                    selectToFilterField("ctl00_mainCopy_Filtro_lsNoPeriodos", "Semestre", 0),
                    radioGroupToFilterField(["ctl00_mainCopy_optActual", "ctl00_mainCopy_optProximo"], ["Periodo actual", "Periodo próximo"], "Periodo"),
                    selectToFilterField("ctl00_mainCopy_lsSecuencias", "Grupo", 0)
                ]);
            """.trimIndent()
        ) {
            val data = it.result.getJSONArray("data")

            List(data.length()) { i ->
                val item = data[i] as JSONObject

                when (FilterType.valueOf(item.getString("type"))) {
                    FilterType.SELECT -> SelectFilterField.fromJson(item)
                    FilterType.RADIO_GROUP -> RadioGroupFilterField.fromJson(item)
                }
            }
        }
    }

    override suspend fun selectSelect(fieldId: String, newIndex: Int?): List<FilterField> {
        return webViewProvider.runThenScrap(
            preRequest = """
            var select = byId("$fieldId");
            
            select.options.selectedIndex = ${newIndex ?: 0};
            select.onchange();
            """.trimIndent(),
            postRequest = """
                next([
                    selectToFilterField("ctl00_mainCopy_Filtro_cboCarrera", "Carrera", 0),
                    selectToFilterField("ctl00_mainCopy_Filtro_cboPlanEstud", "Plan de estudios", 0),
                    selectToFilterField("ctl00_mainCopy_Filtro_cboTurno", "Turno", 0),
                    selectToFilterField("ctl00_mainCopy_Filtro_lsNoPeriodos", "Semestre", 0),
                    radioGroupToFilterField(["ctl00_mainCopy_optActual", "ctl00_mainCopy_optProximo"], ["Periodo actual", "Periodo próximo"], "Periodo"),
                    selectToFilterField("ctl00_mainCopy_lsSecuencias", "Grupo", 0)
                ]);
            """.trimIndent(),
            reloadPage = false
        ) {
            val data = it.result.getJSONArray("data")

            List(data.length()) { i ->
                val item = data[i] as JSONObject

                when (FilterType.valueOf(item.getString("type"))) {
                    FilterType.SELECT -> SelectFilterField.fromJson(item)
                    FilterType.RADIO_GROUP -> RadioGroupFilterField.fromJson(item)
                }
            }
        }
    }

    override suspend fun selectRadioGroup(fieldId: String): List<FilterField> {
        return webViewProvider.runThenScrap(
            preRequest = "byId(\"$fieldId\").click();",
            postRequest = """
                next([
                    selectToFilterField("ctl00_mainCopy_Filtro_cboCarrera", "Carrera", 0),
                    selectToFilterField("ctl00_mainCopy_Filtro_cboPlanEstud", "Plan de estudios", 0),
                    selectToFilterField("ctl00_mainCopy_Filtro_cboTurno", "Turno", 0),
                    selectToFilterField("ctl00_mainCopy_Filtro_lsNoPeriodos", "Semestre", 0),
                    radioGroupToFilterField(["ctl00_mainCopy_optActual", "ctl00_mainCopy_optProximo"], ["Periodo actual", "Periodo próximo"], "Periodo"),
                    selectToFilterField("ctl00_mainCopy_lsSecuencias", "Grupo", 0)
                ]);
            """.trimIndent(),
            reloadPage = false
        ) {
            val data = it.result.getJSONArray("data")

            List(data.length()) { i ->
                val item = data[i] as JSONObject

                when (FilterType.valueOf(item.getString("type"))) {
                    FilterType.SELECT -> SelectFilterField.fromJson(item)
                    FilterType.RADIO_GROUP -> RadioGroupFilterField.fromJson(item)
                }
            }
        }
    }
}