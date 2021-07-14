package ziox.ramiro.saes.features.saes.features.grades.data.repositories

import android.content.Context
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.json.JSONObject
import ziox.ramiro.saes.data.data_providers.WebViewProvider
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.features.grades.data.models.ClassGrades
import ziox.ramiro.saes.utils.isNetworkAvailable
import ziox.ramiro.saes.utils.runOnDefaultThread
import ziox.ramiro.saes.utils.toProperCase

interface GradesRepository {
    suspend fun getMyGrades() : List<ClassGrades>
}

class GradesWebViewRepository(
    private val context: Context
) : GradesRepository {
    private val webView = WebViewProvider(context, "/Alumnos/Informacion_semestral/calificaciones_sem.aspx")
    private val persistenceRepository = LocalAppDatabase.invoke(context).gradesRepository()

    override suspend fun getMyGrades(): List<ClassGrades> {
        return if (context.isNetworkAvailable()){
            webView.scrap(
                script = """
                var gradesTable = byId("ctl00_mainCopy_GV_Calif");
                var requireTeacherRate = document.getElementById("ctl00_mainCopy_Btn_Evaluar") != null;
                
                if(gradesTable != null){
                    var grades = gradesTable.children[0];
                    grades.splice(0,1);
                    next({
                        grades: grades.map((value) => ({
                            className: value[1],
                            p1: parseInt(value[2]),
                            p2: parseInt(value[3]),
                            p3: parseInt(value[4]),
                            extra: parseInt(value[5]),
                            final: parseInt(value[6]),
                        })),
                        requireTeacherRate: requireTeacherRate
                    });
                }else{
                    next({
                        grades: [],
                        requireTeacherRate: requireTeacherRate
                    });
                }
            """.trimIndent()
            ){
                val data = it.result.getJSONObject("data").getJSONArray("grades")

                List(data.length()){ i ->
                    val item = data[i] as JSONObject
                    ClassGrades(
                        item.getString("className").toProperCase(),
                        item.get("p1") as? Int,
                        item.get("p2") as? Int,
                        item.get("p3") as? Int,
                        item.get("extra") as? Int,
                        item.get("final") as? Int,
                    )
                }
            }.also {
                runOnDefaultThread {
                    persistenceRepository.removeAll()
                    persistenceRepository.addGrades(it)
                }
            }
        }else{
            runOnDefaultThread {
                persistenceRepository.getMyGrades()
            }
        }
    }
}


@Dao
interface GradesRoomRepository {
    @Query("SELECT * FROM class_grades")
    fun getMyGrades() : List<ClassGrades>

    @Insert
    fun addGrades(grades: List<ClassGrades>)

    @Query("DELETE FROM class_grades")
    fun removeAll()
}