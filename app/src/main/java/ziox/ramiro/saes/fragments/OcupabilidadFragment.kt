package ziox.ramiro.saes.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
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
import kotlinx.android.synthetic.main.fragment_ocupabilidad_de_horario.view.*
import kotlinx.android.synthetic.main.view_ocupabilidad_horario_item.view.*
import org.json.JSONArray
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.utils.createWebView
import ziox.ramiro.saes.utils.getUrl
import ziox.ramiro.saes.utils.addBottomInsetPadding
import ziox.ramiro.saes.utils.toProperCase

/**
 * Creado por Ramiro el 2/1/2019 a las 8:25 PM para SAESv2.
 */
class OcupabilidadFragment : Fragment() {
    lateinit var rootView: View
    lateinit var bottomSheet: BottomSheetBehavior<LinearLayout>
    lateinit var ocupabilidadWebView: WebView
    private val crashlytics = FirebaseCrashlytics.getInstance()

    private var canSelectGrupo = true
    private var isGrupoSelected = false
    private var isLoading = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_ocupabilidad_de_horario, container, false)
        bottomSheet = BottomSheetBehavior.from(rootView.bottomSheetFiltro)
        var proc = 0

        rootView.ocupabilidadParent.addBottomInsetPadding()
        rootView.bottomSheetScrollOcupabilidad.addBottomInsetPadding()

        rootView.spinnerGrupo.isSelectOnInitEnable = false
        rootView.spinnerGrupo.padStart = 1
        rootView.spinnerSemestre.isKeepIndexEnable = true

        ocupabilidadWebView = createWebView(activity, object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                when (proc) {
                    0 -> view?.loadUrl(
                        "javascript: document.getElementById(\"ctl00_mainCopy_rblEsquema_0\").click();"
                    )
                    1 -> view?.loadUrl(
                        "javascript: document.getElementById(\"ctl00_mainCopy_Chkespecialidad\").click();"
                    )
                    2 -> view?.loadUrl(
                        "javascript: document.getElementById(\"ctl00_mainCopy_ChkSemestre\").click();"
                    )
                    3 -> view?.loadUrl(
                        "javascript: document.getElementById(\"ctl00_mainCopy_Chkgrupo\").click();"
                    )
                    else -> {
                        if (canSelectGrupo) {
                            view?.loadUrl(
                                "javascript: " +
                                        "document.getElementById(\"ctl00_mainCopy_dpdgrupo\").selectedIndex = 1;" +
                                        "__doPostBack('ctl00\$mainCopy\$dpdgrupo','')"
                            )
                            canSelectGrupo = false
                        } else {
                            clear()
                            view?.loadUrl(
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
                                        "window.JSI.setFiltros(JSON.stringify(carr), JSON.stringify(plan), JSON.stringify(espe), JSON.stringify(sems), JSON.stringify(grup));"
                            )
                            if (isGrupoSelected) {
                                view?.loadUrl(
                                    "javascript: " +
                                            "if(document.getElementById(\"ctl00_mainCopy_GrvOcupabilidad\") != null){" +
                                            "    for(var i = 1 ; i < document.getElementById(\"ctl00_mainCopy_GrvOcupabilidad\").children[0].children.length ; i++)" +
                                            "        window.JSI.addMateria(document.getElementById(\"ctl00_mainCopy_GrvOcupabilidad\").children[0].children[i].children[2].innerText," +
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

        ocupabilidadWebView.addJavascriptInterface(JSI(), "JSI")

        ocupabilidadWebView.loadUrl(getUrl(activity) + "Academica/Ocupabilidad_grupos.aspx")

        bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN

        (activity as SAESActivity?)?.showFab(
            R.drawable.ic_filter_list_white_24dp,
            View.OnClickListener {
                crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                if (bottomSheet.state == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
                } else {
                    bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
                }
            },
            BottomAppBar.FAB_ALIGNMENT_MODE_END
        )

        return rootView
    }

    fun clear() {
        rootView.mainOcupabilidad.removeAllViews()
    }

    inner class JSI {
        private var canUpdateCarrera = true
        private var canUpdatePlanDeEstudios = true
        private var canUpdateEspecialidad = true
        private var canUpdateSemestre = true
        private var canUpdateGrupo = true

        @JavascriptInterface
        fun setFiltros(
            carrera: String,
            planDeEstudios: String,
            especialidad: String,
            semestre: String,
            grupo: String
        ) {
            if (activity is SAESActivity) {
                (activity as SAESActivity).hideEmptyText()
            }

            val carr = JSONArray(carrera)
            val plan = JSONArray(planDeEstudios)
            val espe = JSONArray(especialidad)
            val seme = JSONArray(semestre)
            val grup = JSONArray(grupo)

            val c = Array(carr.length()) {
                carr.getString(it).toProperCase()
            }

            val p = Array(plan.length()) {
                plan.getString(it)
            }

            val e = Array(espe.length()) {
                espe.getString(it)
            }

            val s = Array(seme.length()) {
                seme.getString(it)
            }

            val g = Array(grup.length() + 1) {
                if (it == 0) {
                    "Selecciona un grupo"
                } else {
                    grup.getString(it - 1)
                }
            }

            activity?.runOnUiThread {
                isLoading = true

                if (canUpdateCarrera) {
                    rootView.spinnerCarrera.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdatePlanDeEstudios = true
                            canUpdateEspecialidad = true
                            canUpdateSemestre = true
                            canUpdateGrupo = true
                            isGrupoSelected = false

                            if (!isLoading) {
                                ocupabilidadWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_dpdcarrera\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$dpdcarrera','');"
                                )
                            }
                        }
                    }
                    rootView.spinnerCarrera.setOptions(c)
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
                            canUpdateEspecialidad = true
                            canUpdateSemestre = true
                            canUpdateGrupo = true
                            isGrupoSelected = false

                            if (!isLoading) {
                                ocupabilidadWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_dpdplan\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$dpdplan','');"
                                )
                            }
                        }
                    }
                    rootView.spinnerPlanDeEstudios.setOptions(p)
                    canUpdatePlanDeEstudios = false
                }

                if (canUpdateEspecialidad) {
                    rootView.spinnerEspecialidad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateSemestre = true
                            canUpdateGrupo = true
                            isGrupoSelected = false

                            if (!isLoading) {
                                ocupabilidadWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_dpdespecialidad\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$dpdespecialidad','');"
                                )
                            }
                        }
                    }
                    rootView.spinnerEspecialidad.setOptions(e)
                    canUpdateEspecialidad = false
                }

                if (canUpdateSemestre) {
                    rootView.spinnerSemestre.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateGrupo = true
                            isGrupoSelected = false

                            if (!isLoading) {
                                canSelectGrupo = true
                                ocupabilidadWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_dpdsemestre\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$dpdsemestre','');"
                                )
                            }
                        }
                    }
                    rootView.spinnerSemestre.setOptions(s)
                    canUpdateSemestre = false
                }

                if (canUpdateGrupo) {
                    rootView.spinnerGrupo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            if (!isLoading && position > 0) {
                                isGrupoSelected = true
                                ocupabilidadWebView.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_dpdgrupo\").selectedIndex = ${position - 1};" +
                                            "__doPostBack('ctl00\$mainCopy\$dpdgrupo','');"
                                )

                                if (isGrupoSelected) {
                                    bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
                                }
                            } else if (position == 0) {
                                onEmptyLayout("")
                            }
                        }
                    }
                    rootView.spinnerGrupo.setOptions(g)
                    canUpdateGrupo = false
                }
                isLoading = false
            }
        }

        @JavascriptInterface
        fun addMateria(materia: String, inscritos: Int, cupo: Int) {
            activity?.runOnUiThread {
                val card = LayoutInflater.from(activity)
                    .inflate(R.layout.view_ocupabilidad_horario_item, null, false)

                card.ocupabilidadNombre.text = materia.toProperCase()
                card.ocupabilidadDisponible.text = "${inscritos.toString().padStart(2, '0')}/${cupo.toString().padStart(2, '0')}"

                if (cupo > 0 && inscritos < cupo) {
                    card.ocupabilidadDisponible.setBackgroundColor(
                        Color.HSVToColor(
                            floatArrayOf(
                                120f - ((120f * inscritos) / cupo.toFloat()),
                                255f,
                                200f
                            )
                        )
                    )
                } else {
                    card.ocupabilidadDisponible.setBackgroundColor(Color.GRAY)
                }

                rootView.mainOcupabilidad.addView(card)
            }
        }

        @JavascriptInterface
        fun onEmptyLayout(msg : String) {
            Handler().postDelayed({
                if (rootView.mainOcupabilidad.childCount == 0) {
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