package ziox.ramiro.saes.fragments

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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import org.json.JSONArray
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.databinding.FragmentCourseEquivalencesBinding
import ziox.ramiro.saes.databinding.ViewEquivalenceBinding
import ziox.ramiro.saes.utils.addBottomInsetPadding
import ziox.ramiro.saes.utils.createWebView
import ziox.ramiro.saes.utils.getUrl
import ziox.ramiro.saes.utils.toProperCase

/**
 * Creado por Ramiro el 10/03/2019 a las 06:51 PM para SAESv2.
 */
class CourseEquivalencesFragment : Fragment() {
    private lateinit var rootView: FragmentCourseEquivalencesBinding
    private lateinit var courseToCompareBottomSheet: BottomSheetBehavior<LinearLayout>
    private lateinit var courseComparedBottomSheet: BottomSheetBehavior<LinearLayout>
    private lateinit var equivalencesWebView: WebView
    private val crashlytics = FirebaseCrashlytics.getInstance()
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = FragmentCourseEquivalencesBinding.inflate(inflater, container, false)
        courseToCompareBottomSheet = BottomSheetBehavior.from(rootView.equivalencesFilterToCompare)
        courseComparedBottomSheet = BottomSheetBehavior.from(rootView.equivalencesFilterCompared)
        rootView.parent.addBottomInsetPadding()

        Snackbar.make(
            (activity as SAESActivity).getMainLayout(),
            "Nota: Esta sección necesita buena conexión a internet",
            Snackbar.LENGTH_SHORT
        ).show()

        rootView.curriculumToCompareSelector.isKeepIndexEnable = true
        rootView.curriculumComparedSelector.isKeepIndexEnable = true

        equivalencesWebView = createWebView(activity, object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.loadUrl(
                    "javascript: " +
                            "function getData(){" +
                            "   window.JSI.clear();" +
                            "   var row = document.getElementById(\"ctl00_mainCopy_GV_EquivalenciasA\").getElementsByTagName(\"tr\");" +
                            "   if(row.length > 1){" +
                            "       for(var i = 1 ; i < row.length ; i++){" +
                            "           var col = row[i].getElementsByTagName(\"td\");" +
                            "           window.JSI.addData(col[0].innerText, col[1].innerText, col[2].innerText, col[3].innerText);" +
                            "       }" +
                            "   }else{" +
                            "       window.JSI.empty();" +
                            "   }" +
                            "}" +
                            "function updateFiltros(){" +
                            "   var els3 = document.getElementById(\"ctl00_mainCopy_DDLCarrera\").getElementsByTagName(\"option\");" +
                            "   var carr = [];" +
                            "   for(var i = 0 ; i < els3.length ; ++i){" +
                            "       carr.push(els3[i].innerText);" +
                            "   }" +
                            "   var els4 = document.getElementById(\"ctl00_mainCopy_DDLPlan\").getElementsByTagName(\"option\");" +
                            "   var espe = [];" +
                            "   for(var i = 0 ; i < els4.length ; ++i){" +
                            "      espe.push(els4[i].innerText);" +
                            "   }" +
                            "   var els5 = document.getElementById(\"ctl00_mainCopy_DDLEspecialidad\").getElementsByTagName(\"option\");" +
                            "   var plan = [];" +
                            "   for(var i = 0 ; i < els5.length ; ++i){" +
                            "       plan.push(els5[i].innerText);" +
                            "   }" +
                            "   var els0 = document.getElementById(\"ctl00_mainCopy_DDLECarrera\").getElementsByTagName(\"option\");" +
                            "   var carr2 = [];" +
                            "   for(var i = 0 ; i < els0.length ; ++i){" +
                            "       carr2.push(els0[i].innerText);" +
                            "   }" +
                            "   var els1 = document.getElementById(\"ctl00_mainCopy_DDLEPlan\").getElementsByTagName(\"option\");" +
                            "   var espe2 = [];" +
                            "   for(var i = 0 ; i < els1.length ; ++i){" +
                            "       espe2.push(els1[i].innerText);" +
                            "   }" +
                            "   var els2 = document.getElementById(\"ctl00_mainCopy_DDLEEspecialidad\").getElementsByTagName(\"option\");" +
                            "   var plan2 = [];" +
                            "   for(var i = 0 ; i < els2.length ; ++i){" +
                            "       plan2.push(els2[i].innerText);" +
                            "   }" +
                            "   window.JSI.setFilters(JSON.stringify(carr), JSON.stringify(plan), JSON.stringify(espe), JSON.stringify(carr2), JSON.stringify(plan2), JSON.stringify(espe2));" +
                            "   setTimeout(getData, 500);" +
                            "}" +
                            "updateFiltros();"
                )
            }
        }, (activity as SAESActivity?)?.getProgressBar())

        equivalencesWebView.addJavascriptInterface(JSI(), "JSI")

        equivalencesWebView.loadUrl(getUrl(activity) + "Academica/Equivalencias.aspx")

        courseToCompareBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN

        rootView.careerToCompareButton.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            if (courseComparedBottomSheet.state != BottomSheetBehavior.STATE_HIDDEN) courseComparedBottomSheet.state =
                BottomSheetBehavior.STATE_HIDDEN

            courseToCompareBottomSheet.state = if (courseToCompareBottomSheet.state == BottomSheetBehavior.STATE_HIDDEN) {
                BottomSheetBehavior.STATE_COLLAPSED
            } else {
                BottomSheetBehavior.STATE_HIDDEN
            }
        }

        courseComparedBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN

        rootView.careerComparedButton.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            if (courseToCompareBottomSheet.state != BottomSheetBehavior.STATE_HIDDEN) courseToCompareBottomSheet.state =
                BottomSheetBehavior.STATE_HIDDEN

            courseComparedBottomSheet.state = if (courseComparedBottomSheet.state == BottomSheetBehavior.STATE_HIDDEN) {
                BottomSheetBehavior.STATE_COLLAPSED
            } else {
                BottomSheetBehavior.STATE_HIDDEN
            }
        }

        return rootView.root
    }

    inner class JSI {
        private var canUpdateCareerToCompare = true
        private var canUpdateCurriculumToCompare = true
        private var canUpdateSpecialityToCompare = true

        private var canUpdateCareerCompared = true
        private var canUpdateCurriculumCompared = true
        private var canUpdateSpecialityCompared = true

        @JavascriptInterface
        fun setFilters(
            careerToCompare: String,
            specialityToCompare: String,
            curriculumToCompare: String,
            careerCompared: String,
            specialityCompared: String,
            curriculumCompared: String
        ) {

            val careerToCompareJson = JSONArray(careerToCompare)
            val curriculumToCompareJson = JSONArray(curriculumToCompare)
            val specialityToCompareJson = JSONArray(specialityToCompare)

            val careerComparedJson = JSONArray(careerCompared)
            val curriculumComparedJson = JSONArray(curriculumCompared)
            val specialityComparedJson = JSONArray(specialityCompared)

            val careerToCompareArray = Array(careerToCompareJson.length()) {
                careerToCompareJson.getString(it).toProperCase()
            }

            val curriculumToCompareArray = Array(curriculumToCompareJson.length()) {
                curriculumToCompareJson.getString(it)
            }

            val specialityToCompareArray = Array(specialityToCompareJson.length()) {
                specialityToCompareJson.getString(it)
            }

            val careerComparedArray = Array(careerComparedJson.length()) {
                careerComparedJson.getString(it).toProperCase()
            }

            val curriculumComparedArray = Array(curriculumComparedJson.length()) {
                curriculumComparedJson.getString(it)
            }

            val specialityComparedArray = Array(specialityComparedJson.length()) {
                specialityComparedJson.getString(it)
            }

            activity?.runOnUiThread {
                isLoading = true

                if (canUpdateCareerToCompare) {
                    rootView.careerToCompareSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateCurriculumToCompare = true
                            canUpdateSpecialityToCompare = true

                            if (!isLoading) {
                                equivalencesWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_DDLCarrera\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$DDLCarrera','');" +
                                            "setTimeout(updateFiltros, 300);"
                                )
                            }
                        }
                    }
                    rootView.careerToCompareSelector.setOptions(careerToCompareArray)
                    canUpdateCareerToCompare = false
                }

                if (canUpdateCurriculumToCompare) {
                    rootView.curriculumToCompareSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateSpecialityToCompare = true

                            if (!isLoading) {
                                equivalencesWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_DDLPlan\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$DDLPlan','');" +
                                            "setTimeout(updateFiltros, 300);"
                                )
                            }
                        }
                    }
                    rootView.curriculumToCompareSelector.setOptions(curriculumToCompareArray)
                    canUpdateCurriculumToCompare = false
                }

                if (canUpdateSpecialityToCompare) {
                    rootView.specialityToCompareSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            if (!isLoading) {
                                equivalencesWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_DDLEspecialidad\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$DDLEspecialidad','');" +
                                            "setTimeout(updateFiltros, 300);"
                                )
                            }
                        }
                    }
                    rootView.specialityToCompareSelector.setOptions(specialityToCompareArray)
                    canUpdateSpecialityToCompare = false
                }

                if (canUpdateCareerCompared) {
                    rootView.careerComparedSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateCurriculumCompared = true
                            canUpdateSpecialityCompared = true

                            if (!isLoading) {
                                equivalencesWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_DDLECarrera\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$DDLECarrera','');" +
                                            "setTimeout(updateFiltros, 300);"
                                )
                            }
                        }
                    }
                    rootView.careerComparedSelector.setOptions(careerComparedArray)
                    canUpdateCareerCompared = false
                }

                if (canUpdateCurriculumCompared) {
                    rootView.curriculumComparedSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateSpecialityCompared = true

                            if (!isLoading) {
                                equivalencesWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_DDLEPlan\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$DDLEPlan','');" +
                                            "setTimeout(updateFiltros, 300);"
                                )
                            }
                        }
                    }
                    rootView.curriculumComparedSelector.setOptions(curriculumComparedArray)
                    canUpdateCurriculumCompared = false
                }

                if (canUpdateSpecialityCompared) {
                    rootView.specialityComparedSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            if (!isLoading) {
                                equivalencesWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_DDLEEspecialidad\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$DDLEEspecialidad','');" +
                                            "setTimeout(updateFiltros, 300);"
                                )
                            }
                        }
                    }
                    rootView.specialityComparedSelector.setOptions(specialityComparedArray)
                    canUpdateSpecialityCompared = false
                }

                isLoading = false
            }
        }

        @JavascriptInterface
        fun addData(courseToCompareId: String, courseToCompareName: String, courseComparedId: String, courseComparedName: String) {
            if (activity is SAESActivity) {
                (activity as SAESActivity).hideEmptyText()
            }

            activity?.runOnUiThread {
                val data = ViewEquivalenceBinding.inflate(layoutInflater)

                data.careerToCompareTextView.text = rootView.careerToCompareSelector.selectedItem?.text.toString()
                data.careerComparedTextView.text = rootView.careerComparedSelector.selectedItem?.text.toString()

                data.courseToCompareIdTextView.text = courseToCompareId
                data.courseNameToCompareTexView.text = courseToCompareName.toProperCase()
                data.courseComparedIdTextView.text = courseComparedId
                data.courseNameComparedTexView.text = courseComparedName.toProperCase()

                rootView.parent.addView(data.root)
            }
        }

        @JavascriptInterface
        fun clear() {
            activity?.runOnUiThread {
                rootView.parent.removeAllViews()
            }
        }

        @JavascriptInterface
        fun empty() {
            (activity as SAESActivity?)?.showEmptyText("No se encotraron datos")
        }
    }
}