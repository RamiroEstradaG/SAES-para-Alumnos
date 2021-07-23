package ziox.ramiro.saes.features.saes.features.kardex.data.repositories

import android.content.Context
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ziox.ramiro.saes.data.data_providers.WebViewProvider
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.features.kardex.data.models.KardexData
import ziox.ramiro.saes.features.saes.features.kardex.data.models.KardexDataRoom
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.isNetworkAvailable
import ziox.ramiro.saes.utils.runOnDefaultThread

interface KardexRepository {
    suspend fun getMyKardexData() : KardexData
}


class KardexWebViewRepository(
    private val context: Context
) : KardexRepository {
    private val webView = WebViewProvider(context, "/Alumnos/boleta/kardex.aspx")
    private val persistenceRepository = LocalAppDatabase.invoke(context).kardexRepository()

    override suspend fun getMyKardexData(): KardexData {
        val userId = UserPreferences.invoke(context).getPreference(PreferenceKeys.Boleta, "")
        return if(context.isNetworkAvailable()){
            webView.scrap(
                script = """
                var kardexTable = byId("ctl00_mainCopy_Lbl_Kardex");
                
                if(kardexTable != null){
                    var periods = [...kardexTable.getElementsByTagName("table")];
                    
                    next({
                        generalScore: byId("ctl00_mainCopy_Lbl_Promedio").innerText.trim(),
                        careerName: byId("ctl00_mainCopy_Lbl_Carrera").innerText.trim(),
                        userId: byId("ctl00_mainCopy_Lbl_Nombre")
                            .getElementsByTagName("tr")[0]
                            .getElementsByTagName("td")[1].innerText.trim(),
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
                        careerName: byId("ctl00_mainCopy_Lbl_Carrera").innerText.trim(),
                        userId: byId("ctl00_mainCopy_Lbl_Nombre")
                            .getElementsByTagName("tr")[0]
                            .getElementsByTagName("td")[1].innerText.trim(),
                        periods: []
                    });
                }
            """.trimIndent(),
            ){
                KardexDataRoom(
                    userId,
                    it.result
                )
            }.also {
                runOnDefaultThread {
                    persistenceRepository.removeKardexData(userId)
                    persistenceRepository.addKardexData(it)
                }
            }.toKardexData()
        }else{
            runOnDefaultThread {
                persistenceRepository.getMyKardexData(userId)?.toKardexData() ?: KardexData(
                    null,
                    "",
                    userId,
                    emptyList()
                )
            }
        }
    }
}


@Dao
interface KardexRoomRepository{
    @Query("SELECT * FROM kardex WHERE userId = :userId LIMIT 1")
    fun getMyKardexData(userId: String) : KardexDataRoom?

    @Insert
    fun addKardexData(kardexData: KardexDataRoom)

    @Query("DELETE FROM kardex WHERE userId = :userId")
    fun removeKardexData(userId: String)
}