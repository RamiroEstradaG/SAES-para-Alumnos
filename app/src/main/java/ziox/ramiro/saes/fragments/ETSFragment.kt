package ziox.ramiro.saes.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.android.gms.ads.AdRequest
import com.google.android.material.bottomappbar.BottomAppBar
import kotlinx.android.synthetic.main.fragment_ets.view.*
import kotlinx.android.synthetic.main.view_examen_ets_disponible.view.*
import kotlinx.android.synthetic.main.view_examen_ets_inscrito.view.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.utils.*

/**
 * Creado por Ramiro el 12/13/2018 a las 6:39 PM para SAESv2.
 */
class ETSFragment : Fragment() {
    lateinit var rootView: View
    lateinit var inscribirWebView: WebView
    lateinit var calificacionesWebView: WebView
    var isInit = true
    var inicioComprobante = true
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_ets, container, false)
        rootView.etsParent.addBottomInsetPadding()

        rootView.buttonVerEts.setOnClickListener {
            if(activity is SAESActivity){
                (activity as SAESActivity).postNavigationItemSelected(R.id.nav_calendario_ets, false)
            }
        }

        val comprobanteGenerador = createWebView(activity, object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (inicioComprobante) {
                    inicioComprobante = false
                    view?.loadUrl("javascript: document.getElementById(\"aspnetForm\").submit();")
                } else {
                    inicioComprobante = true
                    view?.loadUrl(
                        "javascript: WebForm_DoPostBackWithOptions(new WebForm_PostBackOptions(\"ctl00\$mainCopy\$Comprobante\", \"\", false, \"\", \"reporte.aspx\", false, false));" +
                                "__doPostBack(\"ctl00\$mainCopy\$Comprobante\", \"\");"
                    )
                }
            }
        }, null)

        inscribirWebView = createWebView(activity, object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (isInit) {
                    comprobanteGenerador.loadUrl(getUrl(activity) + "Alumnos/ETS/inscripcion_ets.aspx")
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (isInit) {

                    isInit = false
                    clear()
                    comprobanteGenerador.stopLoading()
                    comprobanteGenerador.loadUrl("javascript: document.getElementById(\"aspnetForm\").submit();")
                    calificacionesWebView.loadUrl(getUrl(activity) + "Alumnos/ETS/calificaciones_ets.aspx")

                    view?.loadUrl(
                        "javascript: " +
                                "if(document.getElementById(\"ctl00_mainCopy_DataList1_ctl00_periodo_etsLabel\") != null) {" +
                                "   window.JSI.setCreditos(parseInt(document.getElementById(\"ctl00_mainCopy_DataList1_ctl00_no_creditosLabel\").innerText)," +
                                "                           parseInt(document.getElementById(\"ctl00_mainCopy_DataList1_ctl00_utilizadosLabel\").innerText));" +
                                "} else {" +
                                "   window.JSI.onEmptyLayout();" +
                                "}" +
                                "if(document.getElementById(\"ctl00_mainCopy_cmbinformacion\") != null){" +
                                "   document.getElementById(\"ctl00_mainCopy_cmbinformacion\").click();" +
                                "}else{" +
                                "   window.JSI.reload();" +
                                "}"
                    )
                } else {
                    isInit = true
                    comprobanteGenerador.loadUrl("javascript:WebForm_DoPostBackWithOptions(new WebForm_PostBackOptions(\"ctl00\$mainCopy\$Comprobante\", \"\", false, \"\", \"reporte.aspx\", false, false))")
                    view?.loadUrl(
                        "javascript:" +
                                "let btns = [];" +
                                "function clickButton(index){" +
                                "   btns[index].click();" +
                                "}" +
                                "if(document.getElementById(\"ctl00_mainCopy_grvinscribirmaterias\") != null){" +
                                "   var el = document.getElementById(\"ctl00_mainCopy_grvinscribirmaterias\").getElementsByTagName(\"tr\");" +
                                "   for(var i = 1 ; i < el.length ; ++i){" +
                                "       window.JSI.addExamenAprovado(el[i].getElementsByTagName(\"td\")[5].innerText, i-1);" +
                                "       btns.push(el[i].getElementsByTagName(\"input\")[0]);" +
                                "   }" +
                                "} else {" +
                                "   window.JSI.onEmptyLayout();" +
                                "}"
                    )
                }
            }
        }, (activity as SAESActivity?)?.getProgressBar())

        calificacionesWebView = createWebView(activity, object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.loadUrl(
                    "javascript: if(document.getElementById(\"ctl00_mainCopy_GridView1\") != null){" +
                            "   window.JSI.showComprobanteFab();" +
                            "   var el = document.getElementById(\"ctl00_mainCopy_GridView1\").getElementsByTagName(\"tr\");" +
                            "   for(var i = 1; i < el.length ; ++i){" +
                            "       window.JSI.addExamenInscrito(el[i].children[3].innerText," +
                            "                                   el[i].children[5].innerText," +
                            "                                   el[i].children[2].innerText);" +
                            "   }" +
                            "}"
                )
            }
        }, null)

        inscribirWebView.addJavascriptInterface(JSI(), "JSI")
        calificacionesWebView.addJavascriptInterface(JSI(), "JSI")

        inscribirWebView.loadUrl(getUrl(activity) + "Alumnos/ETS/inscripcion_ets.aspx")

        return rootView
    }

    private fun clear() {
        rootView.layoutContainerInscritos.removeAllViews()
        rootView.layoutContainerDisponibles.removeAllViews()
    }

    inner class JSI {
        private var creditosDisponibles = 0
        private var isFabVisible = false

        @JavascriptInterface
        fun setCreditos(creditosTotal: Int, creditosUtilizado: Int) {
            activity?.runOnUiThread {
                creditosDisponibles = creditosTotal - creditosUtilizado
                rootView.labelInscripcion.text = "Exámenes disponibles"
            }
        }

        @JavascriptInterface
        fun addExamenAprovado(name: String, index: Int) {
            activity?.runOnUiThread {
                if (activity is SAESActivity) {
                    (activity as SAESActivity).hideEmptyText()
                }

                if(activity?.haveDonated() == false) {
                    if(rootView.adView3.visibility != View.VISIBLE) {
                        rootView.adView3.loadAd(AdRequest.Builder().build())
                    }
                    rootView.adView3.visibility = View.VISIBLE
                }

                rootView.layoutInscribirETS.visibility = View.VISIBLE

                val aprovadoView = layoutInflater.inflate(R.layout.view_examen_ets_disponible, null, false)

                aprovadoView.labelNombreMateria.text = name.toProperCase()

                aprovadoView.buttonInscribirMateria.setOnClickListener {
                    crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                    inscribirWebView.loadUrl("javascript: clickButton($index)")
                }

                rootView.layoutContainerDisponibles.addView(aprovadoView)
            }
        }


        @JavascriptInterface
        fun addExamenInscrito(materia: String, calif: String, clave: String) {
            activity?.runOnUiThread {
                if (activity is SAESActivity) {
                    (activity as SAESActivity).hideEmptyText()
                }

                if(activity?.haveDonated() == false) {
                    if(rootView.adView3.visibility != View.VISIBLE) {
                        rootView.adView3.loadAd(AdRequest.Builder().build())
                    }
                    rootView.adView3.visibility = View.VISIBLE
                }

                rootView.layoutCalificacionesETS.visibility = View.VISIBLE
                rootView.buttonVerEts.visibility = View.VISIBLE

                val view = layoutInflater.inflate(R.layout.view_examen_ets_inscrito, null, false)

                view.labelNombre.text = if (materia.length <= 1) {
                    "Clave: $clave"
                } else {
                    materia.toProperCase()
                }

                view.labelCalificacion.text =
                    if (calif[0].toInt() !in 48..57 && calif.length <= 1) {
                        "Sin calificar"
                    } else {
                        calif
                    }

                if(calif.toIntOrNull() != null && activity != null){
                    if(calif.toInt() < 6){
                        view.labelCalificacion.setTextColor(ContextCompat.getColor(activity!!, R.color.colorHighlight))
                    }
                }

                rootView.layoutContainerInscritos.addView(view)
            }
        }

        @JavascriptInterface
        fun showComprobanteFab() {
            activity?.runOnUiThread {
                if (!isFabVisible) {
                    (activity as SAESActivity?)?.showFab(
                        R.drawable.ic_file_download_white_24dp,
                        View.OnClickListener {
                            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                            activity?.startActivity(
                                Intent(Intent.ACTION_VIEW).setData(
                                    Uri.parse(
                                        "https://www.saes.upiiz.ipn.mx/PDF/Alumnos/ETS/${getBoleta(
                                            activity
                                        )}.pdf"
                                    )
                                )
                            )
                        },
                        BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
                    )
                    isFabVisible = true
                }
            }
        }

        @JavascriptInterface
        fun onEmptyLayout() {
            if (activity is SAESActivity
                && rootView.layoutCalificacionesETS.visibility != View.VISIBLE
                && rootView.layoutInscribirETS.visibility != View.VISIBLE) {
                (activity as SAESActivity).showEmptyText("No tienes examenes disponibles")
            }
        }

        @JavascriptInterface
        fun reload() {
            inscribirWebView.reload()
        }
    }
}