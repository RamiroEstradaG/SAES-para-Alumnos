package ziox.ramiro.saes.features.saes.presentation.features.grades.data.repositories

import android.content.Context
import org.json.JSONObject
import ziox.ramiro.saes.data.data_provider.createWebView
import ziox.ramiro.saes.data.data_provider.scrap
import ziox.ramiro.saes.features.saes.presentation.features.grades.data.models.ClassGrades

interface GradesRepository {
    suspend fun getMyGrades() : List<ClassGrades>
}

class GradesWebViewRepository(
    context: Context
) : GradesRepository {
    private val webView = createWebView(context)

    override suspend fun getMyGrades(): List<ClassGrades> {
        return webView.scrap(
            script = """
                var gradesTable = byId("ctl00_mainCopy_GV_Calif");
                var requireTeacherRate = document.getElementById("ctl00_mainCopy_Btn_Evaluar") != null;
                
                if(gradesTable != null){
                    var grades = gradesTable.children[0];
                    next({
                        grades: grades.splice(0,1).map((value) => ({
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
            """.trimIndent(),
            path = "/Alumnos/Informacion_semestral/calificaciones_sem.aspx"
        ){
            val data = it.result.getJSONObject("data").getJSONArray("grades")

            List(data.length()){ i ->
                val item = data[i] as JSONObject
                ClassGrades(
                    item.getString("className"),
                    item.get("p1") as? Int,
                    item.get("p2") as? Int,
                    item.get("p3") as? Int,
                    item.get("extra") as? Int,
                    item.get("final") as? Int,
                )
            }
        }
    }
}