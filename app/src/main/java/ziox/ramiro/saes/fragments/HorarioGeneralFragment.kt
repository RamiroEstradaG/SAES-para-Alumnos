package ziox.ramiro.saes.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_horario_general.view.*
import org.json.JSONArray
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.CreateHorarioActivity
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.utils.addBottomInsetPadding
import ziox.ramiro.saes.utils.toProperCase
import ziox.ramiro.saes.views.horarioview.HorarioView

/**
 * Creado por Ramiro el 1/14/2019 a las 7:02 PM para SAESv2.
 */
class HorarioGeneralFragment : Fragment() {
    lateinit var bottomSheet: BottomSheetBehavior<LinearLayout>
    lateinit var rootView: View
    private val crashlytics = FirebaseCrashlytics.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_horario_general, container, false)
        bottomSheet = BottomSheetBehavior.from(rootView.bottomSheetFiltro)
        bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
        rootView.bottomSheetHorarioGeneral.addBottomInsetPadding()
        setHasOptionsMenu(true)

        (activity as SAESActivity?)?.showFab(R.drawable.ic_filter_list_black_24dp, View.OnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            bottomSheet.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }, BottomAppBar.FAB_ALIGNMENT_MODE_END)

        rootView.spinnerPeriodo.isSelectOnInitEnable = false
        rootView.spinnerGrupo.isSelectOnInitEnable = false
        rootView.spinnerGrupo.padStart = 1

        rootView.horarioGeneralView.webView.addJavascriptInterface(SpinnerJSI(), "SPINNER")
        rootView.horarioGeneralView.offsetBottom = 1
        rootView.horarioGeneralView.loadData(object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.loadUrl(
                    "javascript: " +
                            "var els = document.getElementById(\"ctl00_mainCopy_Filtro_lsNoPeriodos\").getElementsByTagName(\"option\");" +
                            "var sems = [];" +
                            "for(var i = 0 ; i < els.length ; ++i){" +
                            "    sems.push(els[i].innerText);" +
                            "}" +
                            "var els2 = document.getElementById(\"ctl00_mainCopy_lsSecuencias\").getElementsByTagName(\"option\");" +
                            "var grup = [];" +
                            "for(var i = 0 ; i < els2.length ; ++i){" +
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
                            "window.SPINNER.loadSpinner(JSON.stringify(carr), JSON.stringify(plan), JSON.stringify(turn), JSON.stringify(sems), JSON.stringify(grup));" +
                            "if(document.getElementById(\"ctl00_mainCopy_dbgHorarios\") != null && document.getElementById(\"ctl00_mainCopy_lsSecuencias\").selectedIndex != 0){" +
                            "   for(var i = 1 ; i < document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children.length ; i++)" +
                            "       for(var e = 5; e < 10 ; ++e)" +
                            "           window.${HorarioView.JsiName}.addClase(document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children[i].children[0].innerText+document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children[i].children[0].innerText, e%5," +
                            "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children[i].children[1].innerText," +
                            "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children[i].children[e].innerText," +
                            "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children[i].children[0].innerText," +
                            "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children[i].children[2].innerText," +
                            "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children[i].children[3].innerText," +
                            "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children[i].children[4].innerText);" +
                            "}" +
                            "window.${HorarioView.JsiName}.onHorarioFinished();"
                )
            }
        }, "Academica/horarios.aspx", activity, true)

        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.horario_general_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.nav_generador_horario){
            crashlytics.log("Click en ${resources.getResourceName(item.itemId)} en la clase ${this.javaClass.canonicalName}")
            startActivity(Intent(activity, CreateHorarioActivity::class.java))
        }
        return true
    }

    inner class SpinnerJSI {
        private var canUpdateCarrera = true
        var canUpdatePlanDeEstudios = true
        var canUpdateTurno = true
        var canUpdatePeriodo = true
        var canUpdateGrupo = true

        var isLoading = false

        @JavascriptInterface
        fun loadSpinner(
            carrera: String,
            plan: String,
            turno: String,
            periodo: String,
            grupo: String
        ) {
            val c = JSONArray(carrera)
            val p = JSONArray(plan)
            val t = JSONArray(turno)
            val s = JSONArray(periodo)
            val g = JSONArray(grupo)

            val carreraArr = Array(c.length()) {
                c.getString(it).toProperCase()
            }
            val planArr = Array(p.length()) {
                p.getString(it)
            }
            val turnoArr = Array(t.length()) {
                t.getString(it)
            }
            val periodoArr = Array(s.length()) {
                s.getString(it)
            }
            val grupoArr = Array(g.length()) {
                g.getString(it)
            }

            activity?.runOnUiThread {
                if (canUpdateCarrera) {
                    rootView.spinnerCarrera.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            rootView.horarioGeneralView.clear()

                            canUpdatePeriodo = true
                            canUpdatePlanDeEstudios = true
                            canUpdateGrupo = true
                            canUpdateTurno = true

                            rootView.horarioGeneralView.webView.loadUrl(
                                "javascript: document.getElementById(\"ctl00_mainCopy_Filtro_cboCarrera\").selectedIndex = $position;" +
                                        "__doPostBack('ctl00\$mainCopy\$Filtro\$cboCarrera','');"
                            )
                        }
                    }
                    rootView.spinnerCarrera.setOptions(carreraArr)
                    canUpdateCarrera = false
                }

                if (canUpdatePlanDeEstudios) {
                    rootView.spinnerPlanDeEstudios.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            rootView.horarioGeneralView.clear()

                            canUpdateTurno = true
                            canUpdatePeriodo = true
                            canUpdateGrupo = true

                            rootView.horarioGeneralView.webView.loadUrl(
                                "javascript: document.getElementById(\"ctl00_mainCopy_Filtro_cboPlanEstud\").selectedIndex = $position;" +
                                        "__doPostBack('ctl00\$mainCopy\$Filtro\$cboPlanEstud','');"
                            )
                        }

                    }
                    rootView.spinnerPlanDeEstudios.setOptions(planArr)
                    canUpdatePlanDeEstudios = false
                }

                if (canUpdateTurno) {
                    rootView.spinnerTurno.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            rootView.horarioGeneralView.clear()

                            canUpdatePeriodo = true
                            canUpdateGrupo = true

                            rootView.horarioGeneralView.webView.loadUrl(
                                "javascript: document.getElementById(\"ctl00_mainCopy_Filtro_cboTurno\").selectedIndex = $position;" +
                                        "__doPostBack('ctl00\$mainCopy\$Filtro\$cboTurno','');"
                            )
                        }

                    }
                    rootView.spinnerTurno.setOptions(turnoArr)
                    canUpdateTurno = false
                }

                if (canUpdatePeriodo) {
                    rootView.spinnerPeriodo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            rootView.horarioGeneralView.clear()

                            canUpdateGrupo = true

                            rootView.horarioGeneralView.webView.loadUrl(
                                "javascript: document.getElementById(\"ctl00_mainCopy_Filtro_lsNoPeriodos\").selectedIndex = $position;" +
                                        "__doPostBack('ctl00\$mainCopy\$Filtro\$lsNoPeriodos','');"
                            )
                        }

                    }
                    rootView.spinnerPeriodo.setOptions(periodoArr)
                    canUpdatePeriodo = false
                }

                if (canUpdateGrupo) {
                    isLoading = true
                    rootView.spinnerGrupo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            rootView.horarioGeneralView.clear()

                            rootView.horarioGeneralView.webView.loadUrl(
                                "javascript: document.getElementById(\"ctl00_mainCopy_lsSecuencias\").selectedIndex = $position;" +
                                        "__doPostBack('ctl00\$mainCopy\$lsSecuencias','');"
                            )

                            if (!isLoading) {
                                bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
                            }

                            isLoading = false
                        }

                    }
                    rootView.spinnerGrupo.setOptions(grupoArr)
                    canUpdateGrupo = false
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rootView.horarioGeneralView.closeDatabases()
    }
}