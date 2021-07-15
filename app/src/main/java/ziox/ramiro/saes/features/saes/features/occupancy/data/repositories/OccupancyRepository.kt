package ziox.ramiro.saes.features.saes.features.occupancy.data.repositories

import android.content.Context
import org.json.JSONObject
import ziox.ramiro.saes.data.data_providers.WebViewProvider
import ziox.ramiro.saes.features.saes.data.models.FilterField
import ziox.ramiro.saes.features.saes.data.models.FilterRepository
import ziox.ramiro.saes.features.saes.data.models.SelectFilterField
import ziox.ramiro.saes.features.saes.features.occupancy.data.models.ClassOccupancy
import ziox.ramiro.saes.utils.toProperCase

interface OccupancyRepository : FilterRepository {
    suspend fun getOccupancyData(): List<ClassOccupancy>
    override suspend fun getFilters(): List<FilterField>
    override suspend fun selectFilterField(fieldId: String, newIndex: Int?): List<FilterField>
}

class OccupancyWebViewRepository(
    context: Context
) : OccupancyRepository{
    private val webViewProvider = WebViewProvider(context, "/Academica/Ocupabilidad_grupos.aspx")

    override suspend fun getOccupancyData(): List<ClassOccupancy> {
        return webViewProvider.scrap(
            script = """
                var occupancyTable = byId("ctl00_mainCopy_GrvOcupabilidad");
                
                if(occupancyTable != null){
                    var trs = [...occupancyTable.getElementsByTagName("tr")];
                    
                    trs.splice(0,1);
                    
                    next(trs.map((trEl) => ({
                        id: trEl.children[1].innerText.trim(),
                        group: trEl.children[0].innerText.trim(),
                        className: trEl.children[2].innerText.trim(),
                        semester: trEl.children[3].innerText.trim(),
                        quota: trEl.children[4].innerText.trim(),
                        current: trEl.children[5].innerText.trim()
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

                ClassOccupancy(
                    item.getString("id"),
                    item.getString("group"),
                    item.getString("className").toProperCase(),
                    item.getString("semester").toInt(),
                    item.getString("quota").toInt(),
                    item.getString("current").toInt()
                )
            }
        }
    }

    override suspend fun getFilters(): List<FilterField> {
        return webViewProvider.runMultipleThenScrap(
            preRequests = listOf(
                "byId(\"ctl00_mainCopy_rblEsquema_0\").click();",
                "byId(\"ctl00_mainCopy_Chkespecialidad\").click();",
                "byId(\"ctl00_mainCopy_ChkSemestre\").click();",
                "byId(\"ctl00_mainCopy_Chkgrupo\").click();",
                """
                var select = byId("ctl00_mainCopy_dpdgrupo");
                
                select.options.selectedIndex = 1;
                select.onchange();
                """.trimIndent(),
                """
                var select = byId("ctl00_mainCopy_dpdgrupo");
                
                select.options.selectedIndex = 0;
                select.onchange();
                """.trimIndent(),
            ),
            postRequest = """
                next([
                    selectToFilterField("ctl00_mainCopy_dpdcarrera", "Carrera", 0),
                    selectToFilterField("ctl00_mainCopy_dpdplan", "Plan de estudios", 0),
                    selectToFilterField("ctl00_mainCopy_dpdespecialidad", "Especialidad", 0),
                    selectToFilterField("ctl00_mainCopy_dpdsemestre", "Semestre", 0),
                    selectToFilterField("ctl00_mainCopy_dpdgrupo", "Grupo", 0)
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
                    item.getString("selectedIndex").toIntOrNull(),
                    item.getInt("offset"),
                    List(options.length()){ e ->
                        options[e].toString()
                    }
                )
            }
        }
    }

    override suspend fun selectFilterField(fieldId: String, newIndex: Int?): List<FilterField> {
        val scripts = ArrayList<String>()

        scripts.add(
            """
            var select = byId("$fieldId");
            
            select.options.selectedIndex = ${newIndex ?: 0};
            select.onchange();
            """.trimIndent()
        )

        if(fieldId != "ctl00_mainCopy_dpdgrupo"){
            scripts.addAll(listOf(
                """
                var select = byId("ctl00_mainCopy_dpdgrupo");
                
                select.options.selectedIndex = 1;
                select.onchange();
                """.trimIndent(),
                """
                var select = byId("ctl00_mainCopy_dpdgrupo");
                
                select.options.selectedIndex = 0;
                select.onchange();
                """.trimIndent(),
            ))
        }


        return webViewProvider.runMultipleThenScrap(
            preRequests = scripts,
            postRequest = """
                next([
                    selectToFilterField("ctl00_mainCopy_dpdcarrera", "Carrera", 0),
                    selectToFilterField("ctl00_mainCopy_dpdplan", "Plan de estudios", 0),
                    selectToFilterField("ctl00_mainCopy_dpdespecialidad", "Especialidad", 0),
                    selectToFilterField("ctl00_mainCopy_dpdsemestre", "Semestre", 0),
                    selectToFilterField("ctl00_mainCopy_dpdgrupo", "Grupo", 0)
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
                    item.getString("selectedIndex").toIntOrNull(),
                    item.getInt("offset"),
                    List(options.length()){ e ->
                        options[e].toString()
                    }
                )
            }
        }
    }
}