package ziox.ramiro.saes.features.saes.features.ets_calendar.data.repositories

import android.content.Context
import org.json.JSONObject
import ziox.ramiro.saes.data.data_provider.createWebView
import ziox.ramiro.saes.data.data_provider.runThenScrap
import ziox.ramiro.saes.features.saes.data.models.FilterField
import ziox.ramiro.saes.features.saes.data.models.SelectFilterField
import ziox.ramiro.saes.features.saes.features.ets_calendar.data.models.ETSCalendarEvent
import ziox.ramiro.saes.utils.toProperCase

interface ETSCalendarRepository {
    suspend fun getETSEvents(): List<ETSCalendarEvent>
    suspend fun getFilters(): List<FilterField>
    suspend fun setSelectFilter(fieldId: String, index: Int?): List<FilterField>
}

class ETSCalendarWebViewRepository(
    private val context: Context
) : ETSCalendarRepository {
    private val webView = createWebView(context)

    override suspend fun getETSEvents(): List<ETSCalendarEvent> {
        TODO()
    }

    override suspend fun getFilters(): List<FilterField> {
        return webView.runThenScrap(
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
            """.trimIndent(),
            path = "/Academica/Calendario_ets.aspx"
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
        return webView.runThenScrap(
            preRequest = """
                var select = byId("$fieldId");
                
                select.selectedIndex = ${index ?: 0};
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
            path = "/Academica/Calendario_ets.aspx"
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