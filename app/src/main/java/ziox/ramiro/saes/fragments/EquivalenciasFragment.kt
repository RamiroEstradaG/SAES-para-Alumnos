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
import kotlinx.android.synthetic.main.fragment_equivalencias.view.*
import kotlinx.android.synthetic.main.view_equivalencias.view.*
import org.json.JSONArray
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.utils.createWebView
import ziox.ramiro.saes.utils.getUrl
import ziox.ramiro.saes.utils.toProperCase

/**
 * Creado por Ramiro el 10/03/2019 a las 06:51 PM para SAESv2.
 */
class EquivalenciasFragment : Fragment() {
    private lateinit var rootView: View
    private lateinit var bottomSheet1: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheet2: BottomSheetBehavior<LinearLayout>
    private lateinit var equivalenciasWebView: WebView
    private val crashlytics = FirebaseCrashlytics.getInstance()
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_equivalencias, container, false)
        bottomSheet1 = BottomSheetBehavior.from(rootView.equivalenciasFilter1)
        bottomSheet2 = BottomSheetBehavior.from(rootView.equivalenciasFilter2)

        Snackbar.make(
            (activity as SAESActivity).getMainLayout(),
            "Nota: Esta sección necesita buena conexión a internet",
            Snackbar.LENGTH_SHORT
        ).show()

        rootView.spinnerPlan1.isKeepIndexEnable = true
        rootView.spinnerPlan2.isKeepIndexEnable = true

        equivalenciasWebView = createWebView(activity, object : WebViewClient() {
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
                            "   window.JSI.setFiltros(JSON.stringify(carr), JSON.stringify(plan), JSON.stringify(espe), JSON.stringify(carr2), JSON.stringify(plan2), JSON.stringify(espe2));" +
                            "   setTimeout(getData, 500);" +
                            "}" +
                            "updateFiltros();"
                )
            }
        }, (activity as SAESActivity?)?.getProgressBar())

        equivalenciasWebView.addJavascriptInterface(JSI(), "JSI")

        equivalenciasWebView.loadUrl(getUrl(activity) + "Academica/Equivalencias.aspx")

        bottomSheet1.state = BottomSheetBehavior.STATE_HIDDEN

        rootView.buttonCarrera1.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            if (bottomSheet2.state != BottomSheetBehavior.STATE_HIDDEN) bottomSheet2.state =
                BottomSheetBehavior.STATE_HIDDEN

            bottomSheet1.state = if (bottomSheet1.state == BottomSheetBehavior.STATE_HIDDEN) {
                BottomSheetBehavior.STATE_COLLAPSED
            } else {
                BottomSheetBehavior.STATE_HIDDEN
            }
        }

        bottomSheet2.state = BottomSheetBehavior.STATE_HIDDEN

        rootView.buttonCarrera2.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            if (bottomSheet1.state != BottomSheetBehavior.STATE_HIDDEN) bottomSheet1.state =
                BottomSheetBehavior.STATE_HIDDEN

            bottomSheet2.state = if (bottomSheet2.state == BottomSheetBehavior.STATE_HIDDEN) {
                BottomSheetBehavior.STATE_COLLAPSED
            } else {
                BottomSheetBehavior.STATE_HIDDEN
            }
        }

        return rootView
    }

    inner class JSI {
        private var canUpdateCarrera1 = true
        private var canUpdatePlanDeEstudios1 = true
        private var canUpdateEspecialidad1 = true

        private var canUpdateCarrera2 = true
        private var canUpdatePlanDeEstudios2 = true
        private var canUpdateEspecialidad2 = true

        @JavascriptInterface
        fun setFiltros(
            carrera1: String,
            especialidad1: String,
            pland1: String,
            carrera2: String,
            especialidad2: String,
            pland2: String
        ) {

            val carr1 = JSONArray(carrera1)
            val plan1 = JSONArray(pland1)
            val espe1 = JSONArray(especialidad1)

            val carr2 = JSONArray(carrera2)
            val plan2 = JSONArray(pland2)
            val espe2 = JSONArray(especialidad2)

            val c1 = Array(carr1.length()) {
                carr1.getString(it).toProperCase()
            }

            val p1 = Array(plan1.length()) {
                plan1.getString(it)
            }

            val e1 = Array(espe1.length()) {
                espe1.getString(it)
            }

            val c2 = Array(carr2.length()) {
                carr2.getString(it).toProperCase()
            }

            val p2 = Array(plan2.length()) {
                plan2.getString(it)
            }

            val e2 = Array(espe2.length()) {
                espe2.getString(it)
            }

            activity?.runOnUiThread {
                isLoading = true

                if (canUpdateCarrera1) {
                    rootView.spinnerCarrera1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdatePlanDeEstudios1 = true
                            canUpdateEspecialidad1 = true

                            if (!isLoading) {
                                equivalenciasWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_DDLCarrera\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$DDLCarrera','');" +
                                            "setTimeout(updateFiltros, 300);"
                                )
                            }
                        }
                    }
                    rootView.spinnerCarrera1.setOptions(c1)
                    canUpdateCarrera1 = false
                }

                if (canUpdatePlanDeEstudios1) {
                    rootView.spinnerPlan1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateEspecialidad1 = true

                            if (!isLoading) {
                                equivalenciasWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_DDLPlan\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$DDLPlan','');" +
                                            "setTimeout(updateFiltros, 300);"
                                )
                            }
                        }
                    }
                    rootView.spinnerPlan1.setOptions(p1)
                    canUpdatePlanDeEstudios1 = false
                }

                if (canUpdateEspecialidad1) {
                    rootView.spinnerEspecialidad1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            if (!isLoading) {
                                equivalenciasWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_DDLEspecialidad\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$DDLEspecialidad','');" +
                                            "setTimeout(updateFiltros, 300);"
                                )
                            }
                        }
                    }
                    rootView.spinnerEspecialidad1.setOptions(e1)
                    canUpdateEspecialidad1 = false
                }

                if (canUpdateCarrera2) {
                    rootView.spinnerCarrera2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdatePlanDeEstudios2 = true
                            canUpdateEspecialidad2 = true

                            if (!isLoading) {
                                equivalenciasWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_DDLECarrera\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$DDLECarrera','');" +
                                            "setTimeout(updateFiltros, 300);"
                                )
                            }
                        }
                    }
                    rootView.spinnerCarrera2.setOptions(c2)
                    canUpdateCarrera2 = false
                }

                if (canUpdatePlanDeEstudios2) {
                    rootView.spinnerPlan2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateEspecialidad2 = true

                            if (!isLoading) {
                                equivalenciasWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_DDLEPlan\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$DDLEPlan','');" +
                                            "setTimeout(updateFiltros, 300);"
                                )
                            }
                        }
                    }
                    rootView.spinnerPlan2.setOptions(p2)
                    canUpdatePlanDeEstudios2 = false
                }

                if (canUpdateEspecialidad2) {
                    rootView.spinnerEspecialidad2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            if (!isLoading) {
                                equivalenciasWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_DDLEEspecialidad\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$DDLEEspecialidad','');" +
                                            "setTimeout(updateFiltros, 300);"
                                )
                            }
                        }
                    }
                    rootView.spinnerEspecialidad2.setOptions(e2)
                    canUpdateEspecialidad2 = false
                }

                isLoading = false
            }
        }

        @JavascriptInterface
        fun addData(id: String, nombre: String, id2: String, nombre2: String) {
            if (activity is SAESActivity) {
                (activity as SAESActivity).hideEmptyText()
            }

            activity?.runOnUiThread {
                val data = layoutInflater.inflate(R.layout.view_equivalencias, null, false)

                data.equivalenciasCarrera1.text = rootView.spinnerCarrera1.selectedItem?.text.toString()
                data.equivalenciasCarrera2.text = rootView.spinnerCarrera2.selectedItem?.text.toString()

                data.equivalenciasID1.text = id
                data.equivalenciasMateria1.text = nombre.toProperCase()
                data.equivalenciasID2.text = id2
                data.equivalenciasMateria2.text = nombre2.toProperCase()

                rootView.equivalenciasLayout.addView(data)
            }
        }

        @JavascriptInterface
        fun clear() {
            activity?.runOnUiThread {
                rootView.equivalenciasLayout.removeAllViews()
            }
        }

        @JavascriptInterface
        fun empty() {
            (activity as SAESActivity?)?.showEmptyText("No se encotraron datos")
        }
    }
}