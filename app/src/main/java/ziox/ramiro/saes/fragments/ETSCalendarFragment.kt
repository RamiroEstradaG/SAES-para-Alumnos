package ziox.ramiro.saes.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.databinding.FragmentEtsCalendarBinding
import ziox.ramiro.saes.utils.*
import ziox.ramiro.saes.views.calendar_view.CalendarView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Creado por Ramiro el 1/27/2019 a las 8:08 AM para SAESv2.
 */

class ETSCalendarFragment : Fragment() {
    private lateinit var bottomSheet: BottomSheetBehavior<LinearLayout>
    private lateinit var rootView: FragmentEtsCalendarBinding
    private lateinit var etsCalendarWebView: WebView
    private val crashlytics = FirebaseCrashlytics.getInstance()

    private val etsMap = HashMap<GregorianCalendar, ArrayList<CalendarView.EventData>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = FragmentEtsCalendarBinding.inflate(inflater, container, false)
        bottomSheet = BottomSheetBehavior.from(rootView.filterBottomSheet)
        setSystemUiLightStatusBar(requireActivity(), false)
        rootView.bottomSheetScrollView.addBottomInsetPadding()

        bottomSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState){
                    BottomSheetBehavior.STATE_HIDDEN -> (activity as? SAESActivity)?.showDragIcon()
                    else -> (activity as? SAESActivity)?.hideDragIcon()
                }
            }

        })

        bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN

        rootView.schoolShiftSelector.isKeepIndexEnable = true

        (activity as? SAESActivity)?.setOnDragHorizontaly {
            if (it){
                rootView.etsCalendarView.scrollToPrev()
            }else{
                rootView.etsCalendarView.scrollToNext()
            }
        }

        (activity as? SAESActivity)?.showFab(
            R.drawable.ic_filter_list_white_24dp,
            {
                crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                if (bottomSheet.state == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
                } else {
                    bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
                }
            },
            BottomAppBar.FAB_ALIGNMENT_MODE_END
        )

        etsCalendarWebView = createWebView(activity, object : WebViewClient() {
            private var isPeriodSelected = false

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (isPeriodSelected) {
                    view?.loadUrl(
                        "javascript:" +
                                "var els = document.getElementById(\"ctl00_mainCopy_dpdTipoETSactual\").getElementsByTagName(\"option\");" +
                                "var tipo = [];" +
                                "for(var i = 0 ; i < els.length ; ++i){" +
                                "    tipo.push(els[i].innerText);" +
                                "}" +
                                "var els2 = document.getElementById(\"ctl00_mainCopy_dpdcarrera\").getElementsByTagName(\"option\");" +
                                "var carr = [];" +
                                "for(var i = 0 ; i < els2.length ; ++i){" +
                                "    carr.push(els2[i].innerText);" +
                                "}" +
                                "var els3 = document.getElementById(\"ctl00_mainCopy_dpdplan\").getElementsByTagName(\"option\");" +
                                "var plan = [];" +
                                "for(var i = 0 ; i < els3.length ; ++i){" +
                                "    plan.push(els3[i].innerText);" +
                                "}" +
                                "var els4 = document.getElementById(\"ctl00_mainCopy_dpdespecialidad\").getElementsByTagName(\"option\");" +
                                "var espe = [];" +
                                "for(var i = 0 ; i < els4.length ; ++i){" +
                                "    espe.push(els4[i].innerText);" +
                                "}" +
                                "var els5 = document.getElementById(\"ctl00_mainCopy_dpdSemestre\").getElementsByTagName(\"option\");" +
                                "var seme = [];" +
                                "for(var i = 0 ; i < els5.length ; ++i){" +
                                "    seme.push(els5[i].innerText);" +
                                "}" +
                                "var els6 = document.getElementById(\"ctl00_mainCopy_DpdTurno\").getElementsByTagName(\"option\");" +
                                "var turno = [];" +
                                "for(var i = 0 ; i < els6.length ; ++i){" +
                                "    turno.push(els6[i].innerText);" +
                                "}" +
                                "window.JSI.loadSpinner(JSON.stringify(tipo), JSON.stringify(carr), JSON.stringify(plan), JSON.stringify(espe), JSON.stringify(seme), JSON.stringify(turno));" +
                                "" +
                                "if(document.getElementById(\"ctl00_mainCopy_grvcalendario\") != null){" +
                                "   window.JSI.clear();" +
                                "   let rows = document.getElementById(\"ctl00_mainCopy_grvcalendario\").getElementsByTagName(\"tr\");" +
                                "   for(let i = 1 ; i < rows.length ; i++){" +
                                "       let cols = rows[i].getElementsByTagName(\"td\");" +
                                "       window.JSI.addCourse(cols[3].innerText, cols[1].innerText, cols[5].innerText, cols[6].innerText, cols[4].innerText);" +
                                "   }" +
                                "   window.JSI.onCalendarFinish();" +
                                "}"
                    )
                } else {
                    isPeriodSelected = true
                    view?.loadUrl(
                        "javascript: " +
                                "var els = document.getElementById(\"ctl00_mainCopy_dpdperiodoActual\").getElementsByTagName(\"option\");" +
                                "var v1 = [];" +
                                "for(var i = 0 ; i < els.length ; ++i){" +
                                "    v1.push(els[i].innerText);" +
                                "}" +
                                "document.getElementById(\"ctl00_mainCopy_dpdperiodoActual\").selectedIndex = window.JSI.getLastPeriod(JSON.stringify(v1));" +
                                "__doPostBack('ctl00\$mainCopy\$dpdperiodoActual','');"
                    )
                }
            }
        }, (activity as SAESActivity?)?.getProgressBar())

        etsCalendarWebView.addJavascriptInterface(JSI(), "JSI")
        etsCalendarWebView.loadUrl(getUrl(activity) + "Academica/Calendario_ets.aspx")

        return rootView.root
    }

    inner class JSI {
        private var canUpdateETSType = true
        private var canUpdateCareerName = true
        private var canUpdateCurriculum = true
        private var canUpdateSpeciality = true
        private var canUpdateSemester = true
        private var canUpdateSchoolShift = true

        var isLoading = true

        @JavascriptInterface
        @SuppressLint("SetTextI18n")
        fun getLastPeriod(periods: String): Int {
            val periodsJson = JSONArray(periods)
            var indexResult = -1
            var maxValue = 0.0

            for (i in 0 until periodsJson.length()) {
                val period = periodsJson.getString(i).split(Regex("20"), 2)
                if (period.size == 2) {
                    if (period[1].toInt() + (mesToInt(period[0]) / 12.0) > maxValue) {
                        indexResult = i
                        maxValue = period[1].toInt() + (mesToInt(period[0]) / 12.0)
                    }
                }
            }

            activity?.runOnUiThread {
                rootView.periodTextView.text = MES_COMPLETO[((maxValue % 1) * 12).toInt()] + " del 20" + maxValue.toInt()
            }

            return indexResult
        }

        @JavascriptInterface
        fun loadSpinner(
            etsTypes: String,
            careers: String,
            curriculum: String,
            specialities: String,
            semester: String,
            schoolShift: String
        ) {

            val etsTypesJson = JSONArray(etsTypes)
            val careersJson = JSONArray(careers)
            val curriculumJson = JSONArray(curriculum)
            val specialitiesJson = JSONArray(specialities)
            val semesterJson = JSONArray(semester)
            val schoolShiftsJson = JSONArray(schoolShift)

            val etsTypesArray = Array(etsTypesJson.length()) {
                etsTypesJson.getString(it)
            }
            val careersArray = Array(careersJson.length()) {
                careersJson.getString(it).toProperCase()
            }
            val curriculumArray = Array(curriculumJson.length()) {
                curriculumJson.getString(it)
            }
            val specialitiesArray = Array(specialitiesJson.length()) {
                specialitiesJson.getString(it).toProperCase()
            }
            val semesterArray = Array(semesterJson.length()) {
                semesterJson.getString(it)
            }
            val schoolShiftsArray = Array(schoolShiftsJson.length()) {
                schoolShiftsJson.getString(it).toProperCase()
            }

            activity?.runOnUiThread {
                if (canUpdateETSType) {
                    rootView.etsTypeSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateCareerName = true
                            canUpdateCurriculum = true
                            canUpdateSpeciality = true
                            canUpdateSemester = true
                            canUpdateSchoolShift = true

                            canUpdateETSType = false

                            if (!isLoading) {
                                etsCalendarWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_dpdTipoETSactual\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$dpdTipoETSactual','');"
                                )
                                isLoading = true
                            }
                        }

                    }
                    rootView.etsTypeSelector.setOptions(etsTypesArray)
                }

                if (canUpdateCareerName) {
                    rootView.careerSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateCurriculum = true
                            canUpdateSpeciality = true
                            canUpdateSemester = true
                            canUpdateSchoolShift = true

                            canUpdateCareerName = false

                            if (!isLoading) {
                                etsCalendarWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_dpdcarrera\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$dpdcarrera','');"
                                )
                                isLoading = true
                            }
                        }

                    }
                    rootView.careerSelector.setOptions(careersArray)
                }

                if (canUpdateCurriculum) {
                    rootView.curriculumSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateSpeciality = true
                            canUpdateSemester = true
                            canUpdateSchoolShift = true

                            canUpdateCurriculum = false

                            if (!isLoading) {
                                etsCalendarWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_dpdplan\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$dpdplan','');"
                                )
                                isLoading = true
                            }
                        }
                    }
                    rootView.curriculumSelector.setOptions(curriculumArray)
                }

                if (canUpdateSpeciality) {
                    rootView.specialitySelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateSemester = true
                            canUpdateSchoolShift = true
                            canUpdateSpeciality = false

                            if (!isLoading) {
                                etsCalendarWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_dpdespecialidad\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$dpdespecialidad','');"
                                )
                                isLoading = true
                            }
                        }
                    }
                    rootView.specialitySelector.setOptions(specialitiesArray)
                }

                if (canUpdateSemester) {
                    rootView.semesterSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateSchoolShift = true

                            canUpdateSemester = false

                            if (!isLoading) {
                                etsCalendarWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_dpdSemestre\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$dpdSemestre','');"
                                )
                                isLoading = true
                            }
                        }
                    }
                    rootView.semesterSelector.setOptions(semesterArray)
                }

                if (canUpdateSchoolShift) {
                    rootView.schoolShiftSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateSchoolShift = false

                            if (!isLoading) {
                                etsCalendarWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_DpdTurno\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$DpdTurno','');"
                                )
                                isLoading = true
                            }
                        }

                    }
                    rootView.schoolShiftSelector.setOptions(schoolShiftsArray)
                }

                isLoading = false
            }
        }

        @JavascriptInterface
        fun addCourse(
            date: String,
            courseName: String,
            buildingName: String,
            classroomName: String,
            hour: String
        ) {
            val dateSplit = date.split(" ")

            val hourSplit = hour.substring(0..hour.length-3).split(":")
            val hourInt = hourSplit.first().toInt()
            val minutesInt = hourSplit.last().toInt()
            val meridian = if(hour.substring(hour.length-2 until hour.length) == "PM" && hourInt < 12){
                12
            }else{
                0
            }

            val key = GregorianCalendar(
                dateSplit[2].toInt(),
                MES.indexOf(dateSplit[0].toUpperCase(Locale.ROOT)),
                dateSplit[1].toInt(),
                hourInt+meridian,
                minutesInt,
                0
            )

            if(!etsMap.containsKey(key)){
                etsMap[key] = ArrayList()
            }

            etsMap[key]?.add(CalendarView.EventData(courseName.toProperCase(), buildingName, classroomName))
        }

        @JavascriptInterface
        fun onCalendarFinish() {
            activity?.runOnUiThread {
                val keys = etsMap.keys.toTypedArray()
                keys.sortBy {
                    it.timeInMillis
                }

                var day = -1
                var hour = -1.0
                for(k in keys){
                    val currentDay = k.get(Calendar.DAY_OF_YEAR)
                    val currentHour = k.get(Calendar.HOUR_OF_DAY)+(k.get(Calendar.MINUTE)/60.0)
                    if(currentDay != day){
                        rootView.etsCalendarView.addDay("${k.get(Calendar.DAY_OF_MONTH)} de ${MES_COMPLETO[k.get(Calendar.MONTH)]}")
                        day = currentDay
                        hour = -1.0
                    }

                    if(currentHour != hour){
                        var hourString = k.get(Calendar.HOUR)
                        if(hourString == 0) hourString = 12
                        rootView.etsCalendarView.addHour("${hourString.toString().padStart(2,'0')}:${k.get(Calendar.MINUTE).toString().padStart(2, '0')}", if(k.get(Calendar.AM_PM) == Calendar.AM)"A.M." else "P.M.")
                        hour = currentHour
                    }

                    for(e in etsMap[k]!!){
                        rootView.etsCalendarView.addEvent(e)
                    }
                }

                rootView.etsCalendarView.notifyDataSetChanged()
            }
        }

        @JavascriptInterface
        fun clear() {
            etsMap.clear()
            activity?.runOnUiThread {
                rootView.etsCalendarView.clear()
            }
        }
    }
}