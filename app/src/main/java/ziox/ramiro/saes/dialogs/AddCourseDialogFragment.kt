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
import org.json.JSONArray
import ziox.ramiro.saes.activities.ScheduleGeneratorActivity
import ziox.ramiro.saes.databases.ScheduleGeneratorClass
import ziox.ramiro.saes.databinding.DialogFragmentAddCourseBinding
import ziox.ramiro.saes.fragments.ClassScheduleFragment
import ziox.ramiro.saes.utils.createWebView
import ziox.ramiro.saes.utils.getUrl
import ziox.ramiro.saes.utils.toProperCase

/**
 * Creado por Ramiro el 1/22/2019 a las 12:05 AM para SAESv2.
 */

class AddCourseDialogFragment : DialogFragment(){
    lateinit var rootView : DialogFragmentAddCourseBinding
    lateinit var scheduleWebView : WebView
    var coursePosition = 0
    var careerPosition = 0
    var periodPosition = 0
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Dialog_NoActionBar_MinWidth)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = DialogFragmentAddCourseBinding.inflate(inflater, container, false)

        rootView.periodSelector.isSelectOnInitEnable = false
        rootView.groupSelector.isSelectOnInitEnable = false
        rootView.courseNameSelector.isSelectOnInitEnable = false

        scheduleWebView = createWebView(activity, object : WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                rootView.progressBar.visibility = View.VISIBLE
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                rootView.progressBar.visibility = View.INVISIBLE
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
        
        scheduleWebView.addJavascriptInterface(SpinnerJSI(), "SPINNER")
        scheduleWebView.loadUrl(getUrl(activity)+"Academica/horarios.aspx")

        rootView.addCourseButton.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            if(rootView.courseNameSelector.selectedIndex != -1){
                scheduleWebView.loadUrl("javascript:"+
                        "if(document.getElementById(\"ctl00_mainCopy_dbgHorarios\") != null && document.getElementById(\"ctl00_mainCopy_lsSecuencias\").selectedIndex != 0){" +
                        "   window.SPINNER.addCourse(document.getElementById(\"ctl00_mainCopy_Filtro_cboCarrera\").options[$careerPosition].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_Filtro_lsNoPeriodos\").options[$periodPosition].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children [$coursePosition].children[0].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children [$coursePosition].children[1].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children[$coursePosition].children[2].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children [$coursePosition].children[3].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children [$coursePosition].children[4].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children [$coursePosition].children[5].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children [$coursePosition].children[6].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children [$coursePosition].children[7].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children [$coursePosition].children[8].innerText," +
                        "               document.getElementById(\"ctl00_mainCopy_dbgHorarios\").children[0].children [$coursePosition].children[9].innerText);" +
                        "}")
            }
        }
        
        return rootView.root
    }

    inner class SpinnerJSI{
        private var canUpdateCareerSelector = true
        var canUpdateCurriculumSelector = true
        var canUpdateSchoolShiftSelector = true
        var canUpdatePeriod = true
        var canUpdateGroupSelector = true
        var canUpdateCourseNameSelector = true

        @JavascriptInterface
        fun addCourse(career : String, semester : Int, group : String, courseName: String, teacherName : String, buildingName : String,
                      classroomName : String, monday : String, tuesday : String, wednesday : String, thursday : String, friday : String){
            val data = ScheduleGeneratorClass(career, semester, group, courseName, teacherName, buildingName, classroomName, monday, tuesday, wednesday, thursday, friday)

            if(activity is ScheduleGeneratorActivity){
                (activity as ScheduleGeneratorActivity?)?.addItemAndDismiss(data)
            }else if(activity?.supportFragmentManager?.fragments?.first() is ClassScheduleFragment){
                (activity?.supportFragmentManager?.fragments?.first() as ClassScheduleFragment).rootView.classScheduleView.addNewClass(data)
                this@AddCourseDialogFragment.dismiss()
            }
        }

        @JavascriptInterface
        fun loadSpinner(careers: String, curriculum: String, schoolShifts: String, periods: String, groups: String, courseNames: String){
            val careersJson = JSONArray(careers)
            val curriculumJson = JSONArray(curriculum)
            val schoolShiftsJson = JSONArray(schoolShifts)
            val periodsJson = JSONArray(periods)
            val groupsJson = JSONArray(groups)
            val courseNamesJson = JSONArray(courseNames)

            val careersArray = Array(careersJson.length()){
                careersJson.getString(it).toProperCase()
            }
            val curriculumArray = Array(curriculumJson.length()){
                curriculumJson.getString(it)
            }
            val schoolShiftsArray = Array(schoolShiftsJson.length()){
                schoolShiftsJson.getString(it)
            }
            val periodsArray = Array(periodsJson.length()){
                periodsJson.getString(it)
            }
            val groupArray = Array(groupsJson.length()){
                groupsJson.getString(it)
            }
            val courseNamesArray = Array(courseNamesJson.length()){
                courseNamesJson.getString(it).toProperCase()
            }

            activity?.runOnUiThread {
                if(canUpdateCareerSelector){
                    rootView.careerSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            canUpdateCurriculumSelector = true

                            scheduleWebView.loadUrl("javascript: document.getElementById(\"ctl00_mainCopy_Filtro_cboCarrera\").selectedIndex = $position;" +
                                    "__doPostBack('ctl00\$mainCopy\$Filtro\$cboCarrera','');")

                            careerPosition = position
                        }
                    }
                    rootView.careerSelector.setOptions(careersArray)
                    canUpdateCareerSelector = false
                }

                if(canUpdateCurriculumSelector){
                    rootView.curriculumSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            canUpdateSchoolShiftSelector = true

                            scheduleWebView.loadUrl("javascript: document.getElementById(\"ctl00_mainCopy_Filtro_cboPlanEstud\").selectedIndex = $position;" +
                                    "__doPostBack('ctl00\$mainCopy\$Filtro\$cboPlanEstud','');")
                        }

                    }
                    rootView.curriculumSelector.setOptions(curriculumArray)
                    canUpdateCurriculumSelector = false
                }

                if(canUpdateSchoolShiftSelector){
                    rootView.schoolShiftSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            canUpdatePeriod = true
                            rootView.periodSelector.clean()
                            rootView.groupSelector.clean()
                            rootView.courseNameSelector.clean()

                            scheduleWebView.loadUrl("javascript: document.getElementById(\"ctl00_mainCopy_Filtro_cboTurno\").selectedIndex = $position;" +
                                    "__doPostBack('ctl00\$mainCopy\$Filtro\$cboTurno','');")
                        }

                    }
                    rootView.schoolShiftSelector.setOptions(schoolShiftsArray)
                    canUpdateSchoolShiftSelector = false
                }

                if(canUpdatePeriod){
                    rootView.periodSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            canUpdateGroupSelector = true
                            rootView.groupSelector.clean()
                            rootView.courseNameSelector.clean()
                            scheduleWebView.loadUrl("javascript: document.getElementById(\"ctl00_mainCopy_Filtro_lsNoPeriodos\").selectedIndex = $position;" +
                                    "__doPostBack('ctl00\$mainCopy\$Filtro\$lsNoPeriodos','');")

                            periodPosition = position
                        }

                    }
                    rootView.periodSelector.setOptions(periodsArray)
                    canUpdatePeriod = false
                }

                if (canUpdateGroupSelector){
                    rootView.groupSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            canUpdateCourseNameSelector = true
                            rootView.courseNameSelector.clean()
                            scheduleWebView.loadUrl("javascript: document.getElementById(\"ctl00_mainCopy_lsSecuencias\").selectedIndex = $position+1;" +
                                    "__doPostBack('ctl00\$mainCopy\$lsSecuencias','');")
                        }

                    }
                    rootView.groupSelector.setOptions(groupArray)
                    canUpdateGroupSelector = false
                }

                if (canUpdateCourseNameSelector){
                    rootView.courseNameSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            coursePosition = position+1
                        }
                    }
                    rootView.courseNameSelector.setOptions(courseNamesArray)
                    canUpdateCourseNameSelector = false
                }
            }
        }
    }
}