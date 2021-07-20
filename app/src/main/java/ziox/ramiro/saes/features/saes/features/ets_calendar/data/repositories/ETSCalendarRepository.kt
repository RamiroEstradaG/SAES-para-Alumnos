package ziox.ramiro.saes.features.saes.features.ets_calendar.data.repositories

import android.content.Context
import org.json.JSONObject
import ziox.ramiro.saes.data.data_providers.WebViewProvider
import ziox.ramiro.saes.features.saes.data.models.FilterField
import ziox.ramiro.saes.features.saes.data.models.FilterRepository
import ziox.ramiro.saes.features.saes.data.models.SelectFilterField
import ziox.ramiro.saes.features.saes.features.ets_calendar.data.models.ETSCalendarItem
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ShortDate
import ziox.ramiro.saes.utils.hhmma_toHour
import ziox.ramiro.saes.utils.toProperCase

interface ETSCalendarRepository : FilterRepository {
    suspend fun getETSEvents(): List<ETSCalendarItem>
}

class ETSCalendarWebViewRepository(
    context: Context
) : ETSCalendarRepository {
    private val webViewProvider = WebViewProvider(context, "/Academica/Calendario_ets.aspx")

    override suspend fun getETSEvents(): List<ETSCalendarItem> {
        return webViewProvider.scrap(
            script = """
                var calendarTable = byId("ctl00_mainCopy_grvcalendario");
                
                if(calendarTable != null){
                    var trs = [...calendarTable.getElementsByTagName("tr")];
                    
                    trs.splice(0,1);
                    
                    next(trs.map((trEl) => ({
                        id: trEl.children[0].innerText.trim(),
                        className: trEl.children[1].innerText.trim(),
                        date: trEl.children[3].innerText.trim(),
                        hour: trEl.children[4].innerText.trim(),
                        building: trEl.children[5].innerText.trim(),
                        classroom: trEl.children[6].innerText.trim(),
                    })));
                }else{
                    next([]);
                }
            """.trimIndent(),
            reloadPage = false
        ){
            val data = it.result.getJSONArray("data")

            List(data.length()){ i ->
                val item = data[i] as JSONObject

                ETSCalendarItem(
                    item.getString("id"),
                    item.getString("className").toProperCase(),
                    ShortDate.MMMddyyyy(item.getString("date")),
                    item.getString("hour").hhmma_toHour()!!,
                    item.getString("building"),
                    item.getString("classroom"),
                )
            }
        }
    }

    override suspend fun getFilters(): List<FilterField> {
        return webViewProvider.runThenScrap(
            preRequest = """
                var select = byId("ctl00_mainCopy_dpdperiodoActual");
                
                select.selectedIndex = window.Utils.getRecentETSType(getSelectOptions(select, 0));
                select.onchange();
            """.trimIndent(),
            postRequest = """
                next([
                    selectToFilterField("ctl00_mainCopy_dpdperiodoActual", "Periodo de ETS", 0),
                    selectToFilterField("ctl00_mainCopy_dpdTipoETSactual", "Tipo de ETS", 0),
                    selectToFilterField("ctl00_mainCopy_dpdcarrera", "Carrera", 0),
                    selectToFilterField("ctl00_mainCopy_dpdplan", "Plan de estudios", 0),
                    selectToFilterField("ctl00_mainCopy_dpdespecialidad", "Especialidad", 0),
                    selectToFilterField("ctl00_mainCopy_dpdSemestre", "Semestre", 0),
                    selectToFilterField("ctl00_mainCopy_DpdTurno", "Turno", 0)
                ]);
            """.trimIndent()
        ){
            val data = it.result.getJSONArray("data")

            List(data.length()){ i ->
                val item = data[i] as JSONObject

                SelectFilterField.fromJson(item)
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
                    selectToFilterField("ctl00_mainCopy_dpdperiodoActual", "Periodo de ETS", 0),
                    selectToFilterField("ctl00_mainCopy_dpdTipoETSactual", "Tipo de ETS", 0),
                    selectToFilterField("ctl00_mainCopy_dpdcarrera", "Carrera", 0),
                    selectToFilterField("ctl00_mainCopy_dpdplan", "Plan de estudios", 0),
                    selectToFilterField("ctl00_mainCopy_dpdespecialidad", "Especialidad", 0),
                    selectToFilterField("ctl00_mainCopy_dpdSemestre", "Semestre", 0),
                    selectToFilterField("ctl00_mainCopy_DpdTurno", "Turno", 0)
                ]);
            """.trimIndent(),
            reloadPage = false
        ){
            val data = it.result.getJSONArray("data")

            List(data.length()){ i ->
                val item = data[i] as JSONObject

                SelectFilterField.fromJson(item)
            }
        }
    }

    override suspend fun selectRadioGroup(fieldId: String): List<FilterField> = emptyList()
}