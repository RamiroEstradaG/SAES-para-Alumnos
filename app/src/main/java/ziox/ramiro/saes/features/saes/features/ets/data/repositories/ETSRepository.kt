package ziox.ramiro.saes.features.saes.features.ets.data.repositories

import android.content.Context
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import ziox.ramiro.saes.data.data_provider.createWebView
import ziox.ramiro.saes.data.data_provider.runThenScrap
import ziox.ramiro.saes.data.data_provider.scrap
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.features.ets.data.models.ETS
import ziox.ramiro.saes.features.saes.features.ets.data.models.ETSScore
import ziox.ramiro.saes.utils.isNetworkAvailable
import ziox.ramiro.saes.utils.runOnDefaultThread
import ziox.ramiro.saes.utils.toProperCase

interface ETSRepository {
    suspend fun getAvailableETS() : List<ETS>

    suspend fun getETSScores() : List<ETSScore>

    suspend fun enrollETS(etsIndex: Int): List<ETS>
}

class ETSWebViewRepository(
    private val context: Context
) : ETSRepository {
    private val persistenceRepository = LocalAppDatabase.invoke(context).etsRepository()

    override suspend fun getAvailableETS(): List<ETS> {
        return if(context.isNetworkAvailable()){
            createWebView(context).runThenScrap(
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
            }.also {
                runOnDefaultThread {
                    persistenceRepository.removeAllAvailableETS()
                    persistenceRepository.addAllETS(it)
                }
            }
        }else{
            runOnDefaultThread {
                persistenceRepository.getAvailableETS()
            }
        }
    }

    override suspend fun getETSScores(): List<ETSScore> {
        return if (context.isNetworkAvailable()){
            createWebView(context).scrap(
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
            }.also {
                runOnDefaultThread {
                    persistenceRepository.removeAllScores()
                    persistenceRepository.addAllETSScores(it)
                }
            }
        }else{
            runOnDefaultThread {
                persistenceRepository.getETSScores()
            }
        }
    }

    override suspend fun enrollETS(etsIndex: Int): List<ETS> {


        return getAvailableETS()
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