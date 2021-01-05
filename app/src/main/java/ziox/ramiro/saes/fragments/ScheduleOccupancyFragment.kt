package ziox.ramiro.saes.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import ziox.ramiro.saes.databinding.FragmentScheduleOccupancyBinding
import ziox.ramiro.saes.databinding.ViewScheduleOccupancyItemBinding
import ziox.ramiro.saes.utils.addBottomInsetPadding
import ziox.ramiro.saes.utils.createWebView
import ziox.ramiro.saes.utils.getUrl
import ziox.ramiro.saes.utils.toProperCase

/**
 * Creado por Ramiro el 2/1/2019 a las 8:25 PM para SAESv2.
 */
class ScheduleOccupancyFragment : Fragment() {
    lateinit var rootView: FragmentScheduleOccupancyBinding
    lateinit var bottomSheet: BottomSheetBehavior<LinearLayout>
    lateinit var occupancyWebView: WebView
    private val crashlytics = FirebaseCrashlytics.getInstance()

    private var canSelectGroup = true
    private var isGroupSelected = false
    private var isLoading = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = FragmentScheduleOccupancyBinding.inflate(inflater, container, false)
        bottomSheet = BottomSheetBehavior.from(rootView.filterBottomSheet)
        var proc = 0

        rootView.parent.addBottomInsetPadding()
        rootView.bottomSheetScrollView.addBottomInsetPadding()

        rootView.groupSelector.isSelectOnInitEnable = false
        rootView.groupSelector.padStart = 1
        rootView.semesterSelector.isKeepIndexEnable = true

        occupancyWebView = createWebView(activity, object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                when (proc) {
                    0 -> view.loadUrl(
                        "javascript: document.getElementById(\"ctl00_mainCopy_rblEsquema_0\").click();"
                    )
                    1 -> view.loadUrl(
                        "javascript: document.getElementById(\"ctl00_mainCopy_Chkespecialidad\").click();"
                    )
                    2 -> view.loadUrl(
                        "javascript: document.getElementById(\"ctl00_mainCopy_ChkSemestre\").click();"
                    )
                    3 -> view.loadUrl(
                        "javascript: document.getElementById(\"ctl00_mainCopy_Chkgrupo\").click();"
                    )
                    else -> {
                        if (canSelectGroup) {
                            view.loadUrl(
                                "javascript: " +
                                        "document.getElementById(\"ctl00_mainCopy_dpdgrupo\").selectedIndex = 1;" +
                                        "__doPostBack('ctl00\$mainCopy\$dpdgrupo','')"
                            )
                            canSelectGroup = false
                        } else {
                            clear()
                            view.loadUrl(
                                "javascript: " +
                                        "var els = document.getElementById(\"ctl00_mainCopy_dpdsemestre\").getElementsByTagName(\"option\");" +
                                        "var sems = [];" +
                                        "for(var i = 0 ; i < els.length ; ++i){" +
                                        "    sems.push(els[i].innerText);" +
                                        "}" +
                                        "var els2 = document.getElementById(\"ctl00_mainCopy_dpdgrupo\").getElementsByTagName(\"option\");" +
                                        "var grup = [];" +
                                        "for(var i = 0 ; i < els2.length ; ++i){" +
                                        "    grup.push(els2[i].innerText);" +
                                        "}" +
                                        "var els3 = document.getElementById(\"ctl00_mainCopy_dpdcarrera\").getElementsByTagName(\"option\");" +
                                        "var carr = [];" +
                                        "for(var i = 0 ; i < els3.length ; ++i){" +
                                        "    carr.push(els3[i].innerText);" +
                                        "}" +
                                        "var els4 = document.getElementById(\"ctl00_mainCopy_dpdespecialidad\").getElementsByTagName(\"option\");" +
                                        "var espe = [];" +
                                        "for(var i = 0 ; i < els4.length ; ++i){" +
                                        "    espe.push(els4[i].innerText);" +
                                        "}" +
                                        "var els5 = document.getElementById(\"ctl00_mainCopy_dpdplan\").getElementsByTagName(\"option\");" +
                                        "var plan = [];" +
                                        "for(var i = 0 ; i < els5.length ; ++i){" +
                                        "    plan.push(els5[i].innerText);" +
                                        "}" +
                                        "if(grup.length == 0){" +
                                        "   window.JSI.onEmptyLayout(\"\");" +
                                        "}" +
                                        "window.JSI.setFilters(JSON.stringify(carr), JSON.stringify(plan), JSON.stringify(espe), JSON.stringify(sems), JSON.stringify(grup));"
                            )
                            if (isGroupSelected) {
                                view.loadUrl(
                                    "javascript: " +
                                            "if(document.getElementById(\"ctl00_mainCopy_GrvOcupabilidad\") != null){" +
                                            "    for(var i = 1 ; i < document.getElementById(\"ctl00_mainCopy_GrvOcupabilidad\").children[0].children.length ; i++)" +
                                            "        window.JSI.addItem(document.getElementById(\"ctl00_mainCopy_GrvOcupabilidad\").children[0].children[i].children[2].innerText," +
                                            "                    parseInt(document.getElementById(\"ctl00_mainCopy_GrvOcupabilidad\").children[0].children[i].children[5].innerText)," +
                                            "                    parseInt(document.getElementById(\"ctl00_mainCopy_GrvOcupabilidad\").children[0].children[i].children[4].innerText));" +
                                            "}" +
                                            "window.JSI.onEmptyLayout(\"\");"
                                )
                            }
                        }
                    }
                }
                proc++
            }
        }, (activity as SAESActivity?)?.getProgressBar())

        occupancyWebView.addJavascriptInterface(JSI(), "JSI")

        occupancyWebView.loadUrl(getUrl(activity) + "Academica/Ocupabilidad_grupos.aspx")

        bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN

        (activity as SAESActivity?)?.showFab(
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

        return rootView.root
    }

    fun clear() {
        rootView.mainLayout.removeAllViews()
    }

    inner class JSI {
        private var canUpdateCareerSelector = true
        private var canUpdateCurriculumSelector = true
        private var canUpdateSpecialitySelector = true
        private var canUpdateSemesterSelector = true
        private var canUpdateGroupSelector = true

        @JavascriptInterface
        fun setFilters(
            careers: String,
            curriculum: String,
            specialities: String,
            semesters: String,
            groups: String
        ) {
            if (activity is SAESActivity) {
                (activity as SAESActivity).hideEmptyText()
            }

            val careerJson = JSONArray(careers)
            val curriculumJson = JSONArray(curriculum)
            val specialitiesJson = JSONArray(specialities)
            val semesterJson = JSONArray(semesters)
            val groupsJson = JSONArray(groups)

            val careerArray = Array(careerJson.length()) {
                careerJson.getString(it).toProperCase()
            }
            val curriculumArray = Array(curriculumJson.length()) {
                curriculumJson.getString(it)
            }
            val specialitiesArray = Array(specialitiesJson.length()) {
                specialitiesJson.getString(it)
            }
            val semestersArray = Array(semesterJson.length()) {
                semesterJson.getString(it)
            }
            val groupsArray = Array(groupsJson.length() + 1) {
                if (it == 0) {
                    "Selecciona un grupo"
                } else {
                    groupsJson.getString(it - 1)
                }
            }

            activity?.runOnUiThread {
                isLoading = true

                if (canUpdateCareerSelector) {
                    rootView.careerSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateCurriculumSelector = true
                            canUpdateSpecialitySelector = true
                            canUpdateSemesterSelector = true
                            canUpdateGroupSelector = true
                            isGroupSelected = false

                            if (!isLoading) {
                                occupancyWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_dpdcarrera\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$dpdcarrera','');"
                                )
                            }
                        }
                    }
                    rootView.careerSelector.setOptions(careerArray)
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
                            canUpdateSpecialitySelector = true
                            canUpdateSemesterSelector = true
                            canUpdateGroupSelector = true
                            isGroupSelected = false

                            if (!isLoading) {
                                occupancyWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_dpdplan\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$dpdplan','');"
                                )
                            }
                        }
                    }
                    rootView.curriculumSelector.setOptions(curriculumArray)
                    canUpdateCurriculumSelector = false
                }

                if (canUpdateSpecialitySelector) {
                    rootView.specialitySelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateSemesterSelector = true
                            canUpdateGroupSelector = true
                            isGroupSelected = false

                            if (!isLoading) {
                                occupancyWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_dpdespecialidad\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$dpdespecialidad','');"
                                )
                            }
                        }
                    }
                    rootView.specialitySelector.setOptions(specialitiesArray)
                    canUpdateSpecialitySelector = false
                }

                if (canUpdateSemesterSelector) {
                    rootView.semesterSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateGroupSelector = true
                            isGroupSelected = false

                            if (!isLoading) {
                                canSelectGroup = true
                                occupancyWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_dpdsemestre\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$dpdsemestre','');"
                                )
                            }
                        }
                    }
                    rootView.semesterSelector.setOptions(semestersArray)
                    canUpdateSemesterSelector = false
                }

                if (canUpdateGroupSelector) {
                    rootView.groupSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            if (!isLoading && position > 0) {
                                isGroupSelected = true
                                occupancyWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_dpdgrupo\").selectedIndex = ${position - 1};" +
                                            "__doPostBack('ctl00\$mainCopy\$dpdgrupo','');"
                                )

                                if (isGroupSelected) {
                                    bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
                                }
                            } else if (position == 0) {
                                onEmptyLayout("")
                            }
                        }
                    }
                    rootView.groupSelector.setOptions(groupsArray)
                    canUpdateGroupSelector = false
                }
                isLoading = false
            }
        }

        @JavascriptInterface
        fun addItem(courseName: String, registeredCount: Int, maxCount: Int) {
            activity?.runOnUiThread {
                val card = ViewScheduleOccupancyItemBinding.inflate(layoutInflater)

                card.courseNameTextView.text = courseName.toProperCase()
                card.registeredCountTextView.text = "${registeredCount.toString().padStart(2, '0')}/${maxCount.toString().padStart(2, '0')}"

                if (maxCount > 0 && registeredCount < maxCount) {
                    card.registeredCountTextView.setBackgroundColor(
                        Color.HSVToColor(
                            floatArrayOf(
                                120f - ((120f * registeredCount) / maxCount.toFloat()),
                                255f,
                                200f
                            )
                        )
                    )
                } else {
                    card.registeredCountTextView.setBackgroundColor(Color.GRAY)
                }

                rootView.mainLayout.addView(card.root)
            }
        }

        @JavascriptInterface
        fun onEmptyLayout(msg : String) {
            Handler(Looper.getMainLooper()).postDelayed({
                if (rootView.mainLayout.childCount == 0) {
                    if (activity is SAESActivity) {
                        (activity as SAESActivity).showEmptyText(if(msg.isNotBlank()){
                            msg
                        }else{
                            "Sin datos\nSelecciona otro grupo"
                        })
                    }
                }
            }, 500)
        }
    }
}