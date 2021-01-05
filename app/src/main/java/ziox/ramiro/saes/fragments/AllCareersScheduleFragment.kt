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
import org.json.JSONArray
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.ScheduleGeneratorActivity
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.databinding.FragmentAllCareersScheduleBinding
import ziox.ramiro.saes.utils.addBottomInsetPadding
import ziox.ramiro.saes.utils.toProperCase
import ziox.ramiro.saes.views.schedule_view.ScheduleView

/**
 * Creado por Ramiro el 1/14/2019 a las 7:02 PM para SAESv2.
 */
class AllCareersScheduleFragment : Fragment() {
    lateinit var bottomSheet: BottomSheetBehavior<LinearLayout>
    lateinit var rootView: FragmentAllCareersScheduleBinding
    private val crashlytics = FirebaseCrashlytics.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = FragmentAllCareersScheduleBinding.inflate(inflater, container, false)
        bottomSheet = BottomSheetBehavior.from(rootView.filterBottomSheet)
        bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
        rootView.bottomSheetHorarioGeneral.addBottomInsetPadding()
        setHasOptionsMenu(true)

        (activity as SAESActivity?)?.showFab(R.drawable.ic_filter_list_black_24dp, {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            bottomSheet.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }, BottomAppBar.FAB_ALIGNMENT_MODE_END)

        rootView.periodSelector.isSelectOnInitEnable = false
        rootView.groupSelector.isSelectOnInitEnable = false
        rootView.groupSelector.padStart = 1

        rootView.scheduleView.webView.addJavascriptInterface(SpinnerJSI(), "SPINNER")
        rootView.scheduleView.offsetBottom = 1
        rootView.scheduleView.loadData(object : WebViewClient() {
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
                            "           window.${ScheduleView.JSI_NAME}.addClass(document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children[i].children[0].innerText+document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children[i].children[0].innerText, e%5," +
                            "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children[i].children[1].innerText," +
                            "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children[i].children[e].innerText," +
                            "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children[i].children[0].innerText," +
                            "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children[i].children[2].innerText," +
                            "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children[i].children[3].innerText," +
                            "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children[i].children[4].innerText);" +
                            "}" +
                            "window.${ScheduleView.JSI_NAME}.onScheduleFinished();"
                )
            }
        }, "Academica/horarios.aspx", activity, true)

        return rootView.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.horario_general_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.nav_generador_horario){
            crashlytics.log("Click en ${resources.getResourceName(item.itemId)} en la clase ${this.javaClass.canonicalName}")
            startActivity(Intent(activity, ScheduleGeneratorActivity::class.java))
        }
        return true
    }

    inner class SpinnerJSI {
        private var canUpdateCareerSelector = true
        var canUpdateCurriculumSelector = true
        var canUpdateSchoolShiftSelector = true
        var canUpdatePeriodSelector = true
        var canUpdateGroupSelector = true

        var isLoading = false

        @JavascriptInterface
        fun loadSpinner(
            careers: String,
            curriculum: String,
            schoolShifts: String,
            periods: String,
            groups: String
        ) {
            val careersJson = JSONArray(careers)
            val curriculumJson = JSONArray(curriculum)
            val schoolShiftsJson = JSONArray(schoolShifts)
            val periodsJson = JSONArray(periods)
            val groupsJson = JSONArray(groups)

            val careersArray = Array(careersJson.length()) {
                careersJson.getString(it).toProperCase()
            }
            val curriculumArray = Array(curriculumJson.length()) {
                curriculumJson.getString(it)
            }
            val schoolShiftArray = Array(schoolShiftsJson.length()) {
                schoolShiftsJson.getString(it)
            }
            val periodArray = Array(periodsJson.length()) {
                periodsJson.getString(it)
            }
            val groupArray = Array(groupsJson.length()) {
                groupsJson.getString(it)
            }

            activity?.runOnUiThread {
                if (canUpdateCareerSelector) {
                    rootView.careerSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            rootView.scheduleView.clear()

                            canUpdatePeriodSelector = true
                            canUpdateCurriculumSelector = true
                            canUpdateGroupSelector = true
                            canUpdateSchoolShiftSelector = true

                            rootView.scheduleView.webView.loadUrl(
                                "javascript: document.getElementById(\"ctl00_mainCopy_Filtro_cboCarrera\").selectedIndex = $position;" +
                                        "__doPostBack('ctl00\$mainCopy\$Filtro\$cboCarrera','');"
                            )
                        }
                    }
                    rootView.careerSelector.setOptions(careersArray)
                    canUpdateCareerSelector = false
                }

                if (canUpdateCurriculumSelector) {
                    rootView.curriculumSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            rootView.scheduleView.clear()

                            canUpdateSchoolShiftSelector = true
                            canUpdatePeriodSelector = true
                            canUpdateGroupSelector = true

                            rootView.scheduleView.webView.loadUrl(
                                "javascript: document.getElementById(\"ctl00_mainCopy_Filtro_cboPlanEstud\").selectedIndex = $position;" +
                                        "__doPostBack('ctl00\$mainCopy\$Filtro\$cboPlanEstud','');"
                            )
                        }

                    }
                    rootView.curriculumSelector.setOptions(curriculumArray)
                    canUpdateCurriculumSelector = false
                }

                if (canUpdateSchoolShiftSelector) {
                    rootView.schoolShiftSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            rootView.scheduleView.clear()

                            canUpdatePeriodSelector = true
                            canUpdateGroupSelector = true

                            rootView.scheduleView.webView.loadUrl(
                                "javascript: document.getElementById(\"ctl00_mainCopy_Filtro_cboTurno\").selectedIndex = $position;" +
                                        "__doPostBack('ctl00\$mainCopy\$Filtro\$cboTurno','');"
                            )
                        }

                    }
                    rootView.schoolShiftSelector.setOptions(schoolShiftArray)
                    canUpdateSchoolShiftSelector = false
                }

                if (canUpdatePeriodSelector) {
                    rootView.periodSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            rootView.scheduleView.clear()

                            canUpdateGroupSelector = true

                            rootView.scheduleView.webView.loadUrl(
                                "javascript: document.getElementById(\"ctl00_mainCopy_Filtro_lsNoPeriodos\").selectedIndex = $position;" +
                                        "__doPostBack('ctl00\$mainCopy\$Filtro\$lsNoPeriodos','');"
                            )
                        }

                    }
                    rootView.periodSelector.setOptions(periodArray)
                    canUpdatePeriodSelector = false
                }

                if (canUpdateGroupSelector) {
                    isLoading = true
                    rootView.groupSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            rootView.scheduleView.clear()

                            rootView.scheduleView.webView.loadUrl(
                                "javascript: document.getElementById(\"ctl00_mainCopy_lsSecuencias\").selectedIndex = $position;" +
                                        "__doPostBack('ctl00\$mainCopy\$lsSecuencias','');"
                            )

                            if (!isLoading) {
                                bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
                            }

                            isLoading = false
                        }

                    }
                    rootView.groupSelector.setOptions(groupArray)
                    canUpdateGroupSelector = false
                }
            }
        }
    }
}