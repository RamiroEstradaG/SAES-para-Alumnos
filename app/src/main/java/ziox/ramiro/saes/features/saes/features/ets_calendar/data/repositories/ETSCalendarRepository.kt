package ziox.ramiro.saes.features.saes.features.ets_calendar.data.repositories

import android.content.Context
import androidx.compose.runtime.Composable
import org.json.JSONObject
import ziox.ramiro.saes.data.data_providers.WebViewProvider
import ziox.ramiro.saes.features.saes.data.models.FilterField
import ziox.ramiro.saes.features.saes.data.models.SelectFilterField
import ziox.ramiro.saes.features.saes.features.ets_calendar.data.models.ETSCalendarItem
import ziox.ramiro.saes.utils.hhmma_toHour
import ziox.ramiro.saes.utils.MMMddyyyy_toDate

interface ETSCalendarRepository {
    suspend fun getETSEvents(): List<ETSCalendarItem>
    suspend fun getFilters(): List<FilterField>
    suspend fun setSelectFilter(fieldId: String, index: Int?): List<FilterField>
}

class ETSCalendarWebViewRepository(
    context: Context
) : ETSCalendarRepository {
    private val webViewProvider = WebViewProvider(context, "/Academica/Calendario_ets.aspx")

    @Composable
    fun DebugView() = webViewProvider.WebViewProviderDebugView()

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
                    item.getString("className"),
                    item.getString("date").MMMddyyyy_toDate(),
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
                var options = [...select.getElementsByTagName("option")].map((value) => value.innerText.trim());
                
                select.selectedIndex = window.Utils.getRecentETSType(options);
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
                    selectToFilterField("ctl00_mainCopy_DpdTurno", "Turno", 0),
                ]);
            """.trimIndent()
        ){
            val data = it.result.getJSONArray("data")

            List(data.length()){ i ->
                val item = data[i] as JSONObject
                val options = item.getJSONArray("options")

                SelectFilterField(
                    item.getString("id"),
                    item.getString("name"),
                    item.getInt("selectedIndex"),
                    item.getInt("offset"),
                    List(options.length()){ e ->
                        options[e].toString()
                    }
                )
            }
        }
    }

    override suspend fun setSelectFilter(fieldId: String, index: Int?): List<FilterField> {
        return webViewProvider.runThenScrap(
            preRequest = """
                var select = byId("$fieldId");
                
                select.options.selectedIndex = ${index ?: 0};
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
                    selectToFilterField("ctl00_mainCopy_DpdTurno", "Turno", 0),
                ]);
            """.trimIndent(),
            reloadPage = false
        ){
            val data = it.result.getJSONArray("data")

            List(data.length()){ i ->
                val item = data[i] as JSONObject
                val options = item.getJSONArray("options")

                SelectFilterField(
                    item.getString("id"),
                    item.getString("name"),
                    item.getInt("selectedIndex"),
                    item.getInt("offset"),
                    List(options.length()){ e ->
                        options[e].toString()
                    }
                )
            }
        }
    }

}