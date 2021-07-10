package ziox.ramiro.saes.features.saes.features.ets.data.repositories

import android.content.Context
import kotlinx.coroutines.delay
import org.json.JSONObject
import ziox.ramiro.saes.data.data_provider.createWebView
import ziox.ramiro.saes.data.data_provider.runThenScrap
import ziox.ramiro.saes.data.data_provider.scrap
import ziox.ramiro.saes.features.saes.features.ets.data.models.ETS
import ziox.ramiro.saes.features.saes.features.ets.data.models.ETSScore
import ziox.ramiro.saes.utils.toProperCase

interface ETSRepository {
    suspend fun getAvailableETS() : List<ETS>

    suspend fun getETSScores() : List<ETSScore>

    suspend fun enrollETS(etsIndex: Int): List<ETS>
}

class ETSWebViewRepository(
    private val context: Context
) : ETSRepository {
    override suspend fun getAvailableETS(): List<ETS> {
        return createWebView(context).runThenScrap(
            preRequest = """
                byId("ctl00_mainCopy_cmbinformacion").click();
            """.trimIndent(),
            postRequest = """
                var etsTable = byId("ctl00_mainCopy_Grvmateriasofertadas");
                
                if(etsTable != null){
                    var trs = [...etsTable.getElementsByTagName("tr")];
                    
                    trs.splice(0,1);
                    
                    next(trs.map((trEl, i) => ({
                        id: trEl.children[4].innerText.trim(),
                        name: trEl.children[5].innerText.trim(),
                        index: i
                    })));
                }else{
                    next([]);
                }
            """.trimIndent(),
            path = "/Alumnos/ETS/inscripcion_ets.aspx"
        ){
            val data = it.result.getJSONArray("data")

            List(data.length()){ i ->
                val element = data[i] as JSONObject
                ETS(
                    element.getString("id"),
                    element.getString("name").toProperCase(),
                    element.getInt("index")
                )
            }
        }
    }

    override suspend fun getETSScores(): List<ETSScore> {
        return createWebView(context).scrap(
            script = """
                var etsTable = byId("ctl00_mainCopy_GridView1");
                
                if(etsTable != null){
                    var trs = [...etsTable.getElementsByTagName("tr")];
                    
                    trs.splice(0,1);
                    
                    next(trs.map((trEl) => ({
                        id: trEl.children[2].innerText.trim(),
                        period: trEl.children[0].innerText.trim(),
                        grade: trEl.children[5].innerText.trim(),
                        name: trEl.children[3].innerText.trim()
                    })));
                }else{
                    next([]);
                }
            """.trimIndent(),
            path = "/Alumnos/ETS/calificaciones_ets.aspx"
        ){
            val data = it.result.getJSONArray("data")

            List(data.length()){ i ->
                val element = data[i] as JSONObject
                ETSScore(
                    element.getString("id"),
                    element.getString("period"),
                    element.getString("name").toProperCase(),
                    element.getString("grade").toIntOrNull(),
                )
            }
        }
    }

    override suspend fun enrollETS(etsIndex: Int): List<ETS> {


        return getAvailableETS()
    }

}