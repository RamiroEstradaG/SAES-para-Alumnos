package ziox.ramiro.saes.dialogs

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import androidx.fragment.app.DialogFragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.dialog_anadir_materia.view.*
import kotlinx.android.synthetic.main.fragment_horario.view.*
import org.json.JSONArray
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.CreateHorarioActivity
import ziox.ramiro.saes.fragments.HorarioFragment
import ziox.ramiro.saes.sql.HorarioGeneradoDatabase
import ziox.ramiro.saes.utils.createWebView
import ziox.ramiro.saes.utils.getUrl
import ziox.ramiro.saes.utils.toProperCase

/**
 * Creado por Ramiro el 1/22/2019 a las 12:05 AM para SAESv2.
 */

class AnadirMateriaDialog : DialogFragment(){
    lateinit var rootView : View
    lateinit var buscadorHorarioWebView : WebView
    var materiaPosition = 0
    var carreraPosition = 0
    var periodoPosition = 0
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Dialog_NoActionBar_MinWidth)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.dialog_anadir_materia, container, false)

        rootView.spinnerPeriodo.isSelectOnInitEnable = false
        rootView.spinnerGrupo.isSelectOnInitEnable = false
        rootView.spinnerMateria.isSelectOnInitEnable = false

        buscadorHorarioWebView = createWebView(activity, object : WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                rootView.progressBarAnadirMateria.visibility = View.VISIBLE
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                rootView.progressBarAnadirMateria.visibility = View.INVISIBLE
                view?.loadUrl("javascript: " +
                        "var els = document.getElementById(\"ctl00_mainCopy_Filtro_lsNoPeriodos\").getElementsByTagName(\"option\");" +
                        "var sems = [];" +
                        "for(var i = 0 ; i < els.length ; ++i){" +
                        "    sems.push(els[i].innerText);" +
                        "}" +
                        "var els2 = document.getElementById(\"ctl00_mainCopy_lsSecuencias\").getElementsByTagName(\"option\");" +
                        "var grup = [];" +
                        "for(var i = 1 ; i < els2.length ; ++i){" +
                        "    grup.push(els2[i].innerText);" +
                        "}" +
                        "var els3 = document.getElementById(\"ctl00_mainCopy_Filtro_cboCarrera\").getElementsByTagName(\"option\");" +
                        "var carr = [];" +
                        "for(var i = 0 ; i < els3.length ; ++i){" +
                        "    carr.push(els3[i].innerText);" +
                        "}" +
                        "var els4 = document.getElementById(\"ctl00_mainCopy_Filtro_cboTurno\").getElementsByTagName(\"option\");" +
                        "var turn = [];" +
                        "for(var i = 0 ; i < els4.length ; ++i){" +
                        "    turn.push(els4[i].innerText);" +
                        "}" +
                        "var els5 = document.getElementById(\"ctl00_mainCopy_Filtro_cboPlanEstud\").getElementsByTagName(\"option\");" +
                        "var plan = [];" +
                        "for(var i = 0 ; i < els5.length ; ++i){" +
                        "    plan.push(els5[i].innerText);" +
                        "}" +
                        "var materias = [];" +
                        "if(document.getElementById(\"ctl00_mainCopy_dbgHorarios\") != null && document.getElementById(\"ctl00_mainCopy_lsSecuencias\").selectedIndex != 0){" +
                        "   var els6 = document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children;" +
                        "   for(var i = 1 ; i < els6.length ; ++i){" +
                        "       materias.push(els6[i].children[1].innerText);" +
                        "   }" +
                        "}" +
                        "window.SPINNER.loadSpinner(JSON.stringify(carr), JSON.stringify(plan), JSON.stringify(turn), JSON.stringify(sems), JSON.stringify(grup), JSON.stringify(materias));")
            }
        }, null)
        
        buscadorHorarioWebView.addJavascriptInterface(SpinnerJSI(), "SPINNER")

        buscadorHorarioWebView.loadUrl(getUrl(activity)+"Academica/horarios.aspx")

        rootView.anadirMateriaButton.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            if(rootView.spinnerMateria.selectedIndex != -1){
                buscadorHorarioWebView.loadUrl("javascript:"+
                        "if(document.getElementById(\"ctl00_mainCopy_dbgHorarios\") != null && document.getElementById(\"ctl00_mainCopy_lsSecuencias\").selectedIndex != 0){" +
                        "   window.SPINNER.addMateria(document.getElementById(\"ctl00_mainCopy_Filtro_cboCarrera\").options[$carreraPosition].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_Filtro_lsNoPeriodos\").options[$periodoPosition].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children [$materiaPosition].children[0].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children [$materiaPosition].children[1].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children[$materiaPosition].children[2].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children [$materiaPosition].children[3].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children [$materiaPosition].children[4].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children [$materiaPosition].children[5].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children [$materiaPosition].children[6].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children [$materiaPosition].children[7].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children [$materiaPosition].children[8].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children [$materiaPosition].children[9].innerText);" +
                        "}")
            }
        }
        
        return rootView
    }

    inner class SpinnerJSI{
        private var canUpdateCarrera = true
        var canUpdatePlanDeEstudios = true
        var canUpdateTurno = true
        var canUpdatePeriodo = true
        var canUpdateGrupo = true
        var canUpdateMateria = true

        @JavascriptInterface
        fun addMateria(carrera : String, semestre : Int, grupo : String, materia: String, profesor : String, edificio : String,
                       salon : String, lunes : String, martes : String, miercoles : String, jueves : String, viernes : String){
            val data = HorarioGeneradoDatabase.Data(carrera, semestre, grupo, materia, profesor, edificio, salon, lunes, martes, miercoles, jueves, viernes)

            if(activity is CreateHorarioActivity){
                (activity as CreateHorarioActivity?)?.anadirMateriaAndDimiss(data)
            }else if(activity?.supportFragmentManager?.fragments?.first() is HorarioFragment){
                (activity?.supportFragmentManager?.fragments?.first() as HorarioFragment).rootView.horarioView.newClase(data)
                this@AnadirMateriaDialog.dismiss()
            }
        }

        @JavascriptInterface
        fun loadSpinner(carrera: String, plan: String, turno: String, periodo: String, grupo: String, materia: String){
            val c = JSONArray(carrera)
            val p = JSONArray(plan)
            val t = JSONArray(turno)
            val s = JSONArray(periodo)
            val g = JSONArray(grupo)
            val m = JSONArray(materia)

            val carreraArr = Array(c.length()){
                c.getString(it).toProperCase()
            }
            val planArr = Array(p.length()){
                p.getString(it)
            }
            val turnoArr = Array(t.length()){
                t.getString(it)
            }
            val periodoArr = Array(s.length()){
                s.getString(it)
            }
            val grupoArr = Array(g.length()){
                g.getString(it)
            }
            val materiaArr = Array(m.length()){
                m.getString(it).toProperCase()
            }

            activity?.runOnUiThread {
                if(canUpdateCarrera){
                    rootView.spinnerCarrera.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            canUpdatePlanDeEstudios = true

                            buscadorHorarioWebView.loadUrl("javascript: document.getElementById(\"ctl00_mainCopy_Filtro_cboCarrera\").selectedIndex = $position;" +
                                    "__doPostBack('ctl00\$mainCopy\$Filtro\$cboCarrera','');")

                            carreraPosition = position
                        }
                    }
                    rootView.spinnerCarrera.setOptions(carreraArr)
                    canUpdateCarrera = false
                }

                if(canUpdatePlanDeEstudios){
                    rootView.spinnerPlanDeEstudios.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            canUpdateTurno = true

                            buscadorHorarioWebView.loadUrl("javascript: document.getElementById(\"ctl00_mainCopy_Filtro_cboPlanEstud\").selectedIndex = $position;" +
                                    "__doPostBack('ctl00\$mainCopy\$Filtro\$cboPlanEstud','');")
                        }

                    }
                    rootView.spinnerPlanDeEstudios.setOptions(planArr)
                    canUpdatePlanDeEstudios = false
                }

                if(canUpdateTurno){
                    rootView.spinnerTurno.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            canUpdatePeriodo = true
                            rootView.spinnerPeriodo.clean()
                            rootView.spinnerGrupo.clean()
                            rootView.spinnerMateria.clean()

                            buscadorHorarioWebView.loadUrl("javascript: document.getElementById(\"ctl00_mainCopy_Filtro_cboTurno\").selectedIndex = $position;" +
                                    "__doPostBack('ctl00\$mainCopy\$Filtro\$cboTurno','');")
                        }

                    }
                    rootView.spinnerTurno.setOptions(turnoArr)
                    canUpdateTurno = false
                }

                if(canUpdatePeriodo){
                    rootView.spinnerPeriodo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            canUpdateGrupo = true
                            rootView.spinnerGrupo.clean()
                            rootView.spinnerMateria.clean()
                            buscadorHorarioWebView.loadUrl("javascript: document.getElementById(\"ctl00_mainCopy_Filtro_lsNoPeriodos\").selectedIndex = $position;" +
                                    "__doPostBack('ctl00\$mainCopy\$Filtro\$lsNoPeriodos','');")

                            periodoPosition = position
                        }

                    }
                    rootView.spinnerPeriodo.setOptions(periodoArr)
                    canUpdatePeriodo = false
                }

                if (canUpdateGrupo){
                    rootView.spinnerGrupo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            canUpdateMateria = true
                            rootView.spinnerMateria.clean()
                            buscadorHorarioWebView.loadUrl("javascript: document.getElementById(\"ctl00_mainCopy_lsSecuencias\").selectedIndex = $position+1;" +
                                    "__doPostBack('ctl00\$mainCopy\$lsSecuencias','');")
                        }

                    }
                    rootView.spinnerGrupo.setOptions(grupoArr)
                    canUpdateGrupo = false
                }

                if (canUpdateMateria){
                    rootView.spinnerMateria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            materiaPosition = position+1
                        }
                    }
                    rootView.spinnerMateria.setOptions(materiaArr)
                    canUpdateMateria = false
                }
            }
        }
    }
}