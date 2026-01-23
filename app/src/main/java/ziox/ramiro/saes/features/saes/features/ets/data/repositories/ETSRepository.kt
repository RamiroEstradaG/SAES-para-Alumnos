package ziox.ramiro.saes.features.saes.features.ets.data.repositories

import android.content.Context
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.json.JSONObject
import ziox.ramiro.saes.data.data_providers.Response
import ziox.ramiro.saes.data.data_providers.WebViewProvider
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.features.ets.data.models.ETS
import ziox.ramiro.saes.features.saes.features.ets.data.models.ETSScore
import ziox.ramiro.saes.utils.isNetworkAvailable
import ziox.ramiro.saes.utils.runOnDefaultThread
import ziox.ramiro.saes.utils.toProperCase

interface ETSRepository {
    suspend fun getAvailableETS(): Response<List<ETS>>

    suspend fun getETSScores(): Response<List<ETSScore>>

    suspend fun enrollETS(etsIndex: Int): List<ETS>
}

class ETSWebViewRepository(
    private val context: Context
) : ETSRepository {
    private val persistenceRepository = LocalAppDatabase.invoke(context).etsRepository()
    private val etsWebViewProvider = WebViewProvider(context, "/Alumnos/ETS/inscripcion_ets.aspx")
    private val scoresWebViewProvider =
        WebViewProvider(context, "/Alumnos/ETS/calificaciones_ets.aspx")

    override suspend fun getAvailableETS(): Response<List<ETS>> {
        return if (context.isNetworkAvailable()) {
            etsWebViewProvider.runThenScrap(
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
                """.trimIndent()
            ) {
                val data = it.result.getJSONArray("data")

                List(data.length()) { i ->
                    val element = data[i] as JSONObject
                    ETS(
                        element.getString("id"),
                        element.getString("name").toProperCase(),
                        element.getInt("index")
                    )
                }
            }.also {
                runOnDefaultThread {
                    persistenceRepository.removeAllAvailableETS()
                    persistenceRepository.addAllETS(it.data)
                }
            }
        } else {
            runOnDefaultThread {
                Response(
                    data = persistenceRepository.getAvailableETS(),
                    sourceCode = "local_database",
                    url = "local_database"
                )
            }
        }
    }

    override suspend fun getETSScores(): Response<List<ETSScore>> {
        return if (context.isNetworkAvailable()) {
            scoresWebViewProvider.scrap(
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
            """.trimIndent()
            ) {
                val data = it.result.getJSONArray("data")

                List(data.length()) { i ->
                    val element = data[i] as JSONObject
                    ETSScore(
                        element.getString("id"),
                        element.getString("period"),
                        element.getString("name").toProperCase(),
                        element.getString("grade").toIntOrNull(),
                    )
                }
            }.also {
                runOnDefaultThread {
                    persistenceRepository.removeAllScores()
                    persistenceRepository.addAllETSScores(it.data)
                }
            }
        } else {
            runOnDefaultThread {
                Response(
                    data = persistenceRepository.getETSScores(),
                    sourceCode = "local_database",
                    url = "local_database"
                )
            }
        }
    }

    override suspend fun enrollETS(etsIndex: Int): List<ETS> {
        // TODO: Make this function actually enroll in an ETS

        return getAvailableETS().data
    }
}

@Dao
interface ETSRoomRepository {
    @Query("SELECT * FROM available_ets")
    fun getAvailableETS(): List<ETS>

    @Query("SELECT * FROM ets_scores")
    fun getETSScores(): List<ETSScore>

    @Insert
    fun addAllETS(ets: List<ETS>)

    @Insert
    fun addAllETSScores(scores: List<ETSScore>)

    @Query("DELETE FROM available_ets")
    fun removeAllAvailableETS()

    @Query("DELETE FROM ets_scores")
    fun removeAllScores()
}