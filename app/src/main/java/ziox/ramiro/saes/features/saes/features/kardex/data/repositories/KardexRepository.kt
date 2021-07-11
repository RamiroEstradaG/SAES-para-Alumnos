package ziox.ramiro.saes.features.saes.features.kardex.data.repositories

import android.content.Context
import org.json.JSONObject
import ziox.ramiro.saes.data.data_provider.createWebView
import ziox.ramiro.saes.data.data_provider.scrap
import ziox.ramiro.saes.features.saes.features.kardex.data.models.EvaluationType
import ziox.ramiro.saes.features.saes.features.kardex.data.models.KardexClass
import ziox.ramiro.saes.features.saes.features.kardex.data.models.KardexData
import ziox.ramiro.saes.features.saes.features.kardex.data.models.KardexPeriod
import ziox.ramiro.saes.utils.MMMddyyyy_toDate
import ziox.ramiro.saes.utils.toProperCase

interface KardexRepository {
    suspend fun fetchKardexData() : KardexData
}


class KardexWebViewRepository(
    context: Context
) : KardexRepository {
    private val webView = createWebView(context)

    override suspend fun fetchKardexData(): KardexData {
        return webView.scrap(
            script = """
                var kardexTable = byId("ctl00_mainCopy_Lbl_Kardex");
                
                if(kardexTable != null){
                    var periods = [...kardexTable.getElementsByTagName("table")];
                    
                    next({
                        generalScore: byId("ctl00_mainCopy_Lbl_Promedio").innerText.trim(),
                        periods: periods.map((periodTable) => {
                            var trs = [...periodTable.getElementsByTagName("tr")];
                            var firstRow = trs.splice(0,2)[0];
                            
                            return {
                                periodName: firstRow.innerText.trim(),
                                classes: trs.map(trEl => ({
                                    id: trEl.children[0].innerText.trim(),
                                    name: trEl.children[1].innerText.trim(),
                                    date: trEl.children[2].innerText.trim(),
                                    period: trEl.children[3].innerText.trim(),
                                    evaluationType: trEl.children[4].innerText.trim(),
                                    score: trEl.children[5].innerText.trim(),
                                }))
                            };
                        })
                    });
                }else{
                    next({
                        generalScore: "-",
                        periods: []
                    });
                }
            """.trimIndent(),
            path = "/Alumnos/boleta/kardex.aspx"
        ){
            val data = it.result.getJSONObject("data")
            val periods = data.getJSONArray("periods")

            KardexData(
                data.getString("generalScore").toDoubleOrNull(),
                List(periods.length()){ i ->
                    val period = periods[i] as JSONObject
                    val periodClasses = period.getJSONArray("classes")

                    KardexPeriod(
                        period.getString("periodName").toProperCase(),
                        List(periodClasses.length()){ e ->
                            val kardexClass = periodClasses[e] as JSONObject
                            KardexClass(
                                kardexClass.getString("id"),
                                kardexClass.getString("name").toProperCase(),
                                kardexClass.getString("date").MMMddyyyy_toDate(),
                                kardexClass.getString("period"),
                                EvaluationType.fromSAES(kardexClass.getString("evaluationType")),
                                kardexClass.getString("score").toIntOrNull()
                            )
                        }
                    )
                }
            )
        }
    }

}