package ziox.ramiro.saes.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_horario.*
import kotlinx.android.synthetic.main.fragment_horario.view.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.utils.downloadFile
import ziox.ramiro.saes.utils.getPreference
import ziox.ramiro.saes.utils.isSchoolGroup
import ziox.ramiro.saes.views.horarioview.HorarioView
import java.util.*
import kotlin.collections.ArrayList

/**
 * Creado por Ramiro el 10/14/2018 a las 6:46 PM para SAESv2.
 */
class HorarioFragment : Fragment() {
    lateinit var rootView : View
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_horario, container, false)
        setHasOptionsMenu(true)
        if (activity is SAESActivity) {
            (activity as SAESActivity).showFab(
                R.drawable.ic_add_black_24dp,
                View.OnClickListener {
                    crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                    rootView.horarioView.newClase()
                },
                BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
            )

            (activity as SAESActivity).setOnDragHorizontaly {
                if(!it){
                    rootView.horarioView.nextDay()
                }else{
                    rootView.horarioView.prevDay()
                }
            }

            if(!getPreference(activity, "horario_expand", false)){
                (activity as SAESActivity).hideDragIcon()
            }
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (horarioView != null) {
            horarioView.canEdit = true
            horarioView.isDatabaseEnabled = true
            horarioView.isCurrentDayVisible = true
            horarioView.isCurrentHourVisible = true
            horarioView.offsetBottom = 1

            if (getPreference(context, "horario_expand", false)) {
                val dia = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2
                if (dia in 0..4) {
                    horarioView.toggleDayState(dia)
                }
            }

            horarioView.webView.addJavascriptInterface(ColumnAnalizer(), "ANALIZER")

            horarioView.loadData(
                object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        view?.loadUrl(
                            "javascript: " +
                                    "if(document.getElementById(\"ctl00_mainCopy_GV_Horario\") != null){" +
                                    "   let cols = JSON.parse(window.ANALIZER.analiseColumns([...document.getElementById(\"ctl00_mainCopy_GV_Horario\").children[0].children[0].children].map((el) => el.innerText), " +
                                    "                           [...document.getElementById(\"ctl00_mainCopy_GV_Horario\").children[0].children[1].children].map((el) => el.innerText)));" +
                                    "   for(var i = 1 ; i < document.getElementById(\"ctl00_mainCopy_GV_Horario\").children[0].children.length ; i++)" +
                                    "      for(var e = cols[0]; e <= cols[1] ; ++e)" +
                                    "          window." + HorarioView.JsiName + ".addClase(document.getElementById(\"ctl00_mainCopy_GV_Horario\").children[0].children[i].children[cols[2]].innerText+document.getElementById(\"ctl00_mainCopy_GV_Horario\").children[0].children[i].children[cols[3]].innerText+(e%5).toString(), (e-cols[0])%5," +
                                    "               document.getElementById(\"ctl00_mainCopy_GV_Horario\").children[0].children[i].children[cols[3]].innerText," +
                                    "               document.getElementById(\"ctl00_mainCopy_GV_Horario\").children[0].children[i].children[e].innerText," +
                                    "               document.getElementById(\"ctl00_mainCopy_GV_Horario\").children[0].children[i].children[cols[2]].innerText," +
                                    "               document.getElementById(\"ctl00_mainCopy_GV_Horario\").children[0].children[i].children[cols[4]].innerText," +
                                    "               document.getElementById(\"ctl00_mainCopy_GV_Horario\").children[0].children[i].children[cols[5]].innerText," +
                                    "               document.getElementById(\"ctl00_mainCopy_GV_Horario\").children[0].children[i].children[cols[6]].innerText);" +
                                    "}" +
                                    "window." + HorarioView.JsiName + ".onHorarioFinished();"
                        )
                    }
                },
                "Alumnos/Informacion_semestral/Horario_Alumno.aspx", activity, true
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rootView.horarioView.closeDatabases()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.horario_menu, menu)

        rootView.horarioView.addOnChangeListener { isExpanded, _ ->
            setPaginationVisible(isExpanded)
        }
    }

    fun setPaginationVisible(isVisible : Boolean){
        if(activity is SAESActivity){
            if(isVisible){
                (activity as SAESActivity).showDragIcon()
            }else{
                (activity as SAESActivity).hideDragIcon()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_download_horario -> {
                crashlytics.log("Click en ${resources.getResourceName(item.itemId)} en la clase ${this.javaClass.canonicalName}")
                downloadFile(activity, "ComprobanteHorario")
            }
        }
        return true
    }
}

class ColumnAnalizer {
    @JavascriptInterface
    fun analiseColumns(cols: Array<String>, firstRow: Array<String>) : String{
        val mondayIndex = cols.indexOfFirst {
            it.matches(Regex("lunes", RegexOption.IGNORE_CASE))
        }
        val fridayIndex = cols.indexOfFirst {
            it.matches(Regex("viernes", RegexOption.IGNORE_CASE))
        }
        val buildingIndex = cols.indexOfFirst {
            it.matches(Regex("edificio", RegexOption.IGNORE_CASE))
        }
        val classroomIndex = cols.indexOfFirst {
            it.matches(Regex("sal(o|ó)n", RegexOption.IGNORE_CASE))
        }
        val teacherIndex = cols.indexOfFirst {
            it.matches(Regex("profesor|maestro", RegexOption.IGNORE_CASE))
        }
        val subjectIndex = firstRow.withIndex().indexOfFirst {
            it.value.length >= 7 && it.index != teacherIndex
        }
        val groupIndex = firstRow.withIndex().indexOfFirst {
            it.value.isSchoolGroup() && it.index !in mondayIndex..fridayIndex && it.index != buildingIndex && it.index != classroomIndex && it.index != teacherIndex && it.index != subjectIndex
        }
        return Gson().toJson(arrayOf(mondayIndex, fridayIndex, groupIndex, subjectIndex, teacherIndex, buildingIndex, classroomIndex))
    }
}