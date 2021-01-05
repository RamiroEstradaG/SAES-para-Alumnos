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
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.databinding.FragmentEtsRegisterBinding
import ziox.ramiro.saes.databinding.ViewEtsAvailableBinding
import ziox.ramiro.saes.databinding.ViewEtsRegisteredBinding
import ziox.ramiro.saes.utils.*

/**
 * Creado por Ramiro el 12/13/2018 a las 6:39 PM para SAESv2.
 */
class ETSRegisterFragment : Fragment() {
    lateinit var rootView: FragmentEtsRegisterBinding
    lateinit var registerWebView: WebView
    lateinit var scoresWebView: WebView
    var isInitial = true
    var isVoucherInitialized = true
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = FragmentEtsRegisterBinding.inflate(inflater, container, false)
        rootView.parent.addBottomInsetPadding()

        rootView.etsCalendarButton.setOnClickListener {
            if(activity is SAESActivity){
                (activity as SAESActivity).postNavigationItemSelected(R.id.nav_calendario_ets, false)
            }
        }

        //TODO: No funciona del todo
        val voucherGenerator = createWebView(activity, object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (isVoucherInitialized) {
                    isVoucherInitialized = false
                    view?.loadUrl("javascript: document.getElementById(\"aspnetForm\").submit();")
                } else {
                    isVoucherInitialized = true
                    view?.loadUrl(
                        "javascript: WebForm_DoPostBackWithOptions(new WebForm_PostBackOptions(\"ctl00\$mainCopy\$Comprobante\", \"\", false, \"\", \"reporte.aspx\", false, false));" +
                                "__doPostBack(\"ctl00\$mainCopy\$Comprobante\", \"\");"
                    )
                }
            }
        }, null)

        registerWebView = createWebView(activity, object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (isInitial) {
                    voucherGenerator.loadUrl(getUrl(activity) + "Alumnos/ETS/inscripcion_ets.aspx")
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (isInitial) {

                    isInitial = false
                    clear()
                    voucherGenerator.stopLoading()
                    voucherGenerator.loadUrl("javascript: document.getElementById(\"aspnetForm\").submit();")
                    scoresWebView.loadUrl(getUrl(activity) + "Alumnos/ETS/calificaciones_ets.aspx")

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
                    isInitial = true
                    voucherGenerator.loadUrl("javascript:WebForm_DoPostBackWithOptions(new WebForm_PostBackOptions(\"ctl00\$mainCopy\$Comprobante\", \"\", false, \"\", \"reporte.aspx\", false, false))")
                    view?.loadUrl(
                        "javascript:" +
                                "let btns = [];" +
                                "function clickButton(index){" +
                                "   btns[index].click();" +
                                "}" +
                                "if(document.getElementById(\"ctl00_mainCopy_grvinscribirmaterias\") != null){" +
                                "   var el = document.getElementById(\"ctl00_mainCopy_grvinscribirmaterias\").getElementsByTagName(\"tr\");" +
                                "   for(var i = 1 ; i < el.length ; ++i){" +
                                "       window.JSI.addApprovedExam(el[i].getElementsByTagName(\"td\")[5].innerText, i-1);" +
                                "       btns.push(el[i].getElementsByTagName(\"input\")[0]);" +
                                "   }" +
                                "} else {" +
                                "   window.JSI.onEmptyLayout();" +
                                "}"
                    )
                }
            }
        }, (activity as SAESActivity?)?.getProgressBar())

        scoresWebView = createWebView(activity, object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.loadUrl(
                    "javascript: if(document.getElementById(\"ctl00_mainCopy_GridView1\") != null){" +
                            "   window.JSI.showVoucherFab();" +
                            "   var el = document.getElementById(\"ctl00_mainCopy_GridView1\").getElementsByTagName(\"tr\");" +
                            "   for(var i = 1; i < el.length ; ++i){" +
                            "       window.JSI.addRegisteredExam(el[i].children[3].innerText," +
                            "                                   el[i].children[5].innerText," +
                            "                                   el[i].children[2].innerText);" +
                            "   }" +
                            "}"
                )
            }
        }, null)

        registerWebView.addJavascriptInterface(JSI(), "JSI")
        scoresWebView.addJavascriptInterface(JSI(), "JSI")

        registerWebView.loadUrl(getUrl(activity) + "Alumnos/ETS/inscripcion_ets.aspx")

        return rootView.root
    }

    private fun clear() {
        rootView.registeredContainer.removeAllViews()
        rootView.availableContainer.removeAllViews()
    }

    inner class JSI {
        private var availableCredits = 0
        private var isFabVisible = false

        @JavascriptInterface
        fun setCredits(totalCredits: Int, usedCredits: Int) {
            activity?.runOnUiThread {
                availableCredits = totalCredits - usedCredits
                rootView.registerTitle.text = "Exámenes disponibles"
            }
        }

        @JavascriptInterface
        fun addApprovedExam(name: String, index: Int) {
            activity?.runOnUiThread {
                if (activity is SAESActivity) {
                    (activity as SAESActivity).hideEmptyText()
                }

                if(activity?.haveDonated() == false) {
                    if(rootView.etsRegisterAd.visibility != View.VISIBLE) {
                        rootView.etsRegisterAd.loadAd(AdRequest.Builder().build())
                    }
                    rootView.etsRegisterAd.visibility = View.VISIBLE
                }

                rootView.layoutInscribirETS.visibility = View.VISIBLE

                val approvedView = ViewEtsAvailableBinding.inflate(layoutInflater)

                approvedView.courseNameTextView.text = name.toProperCase()

                approvedView.registerExamButton.setOnClickListener {
                    crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                    registerWebView.loadUrl("javascript: clickButton($index)")
                }

                rootView.availableContainer.addView(approvedView.root)
            }
        }


        @JavascriptInterface
        fun addRegisteredExam(courseName: String, result: String, key: String) {
            activity?.runOnUiThread {
                if (activity is SAESActivity) {
                    (activity as SAESActivity).hideEmptyText()
                }

                if(activity?.haveDonated() == false) {
                    if(rootView.etsRegisterAd.visibility != View.VISIBLE) {
                        rootView.etsRegisterAd.loadAd(AdRequest.Builder().build())
                    }
                    rootView.etsRegisterAd.visibility = View.VISIBLE
                }

                rootView.etsResultsLayout.visibility = View.VISIBLE
                rootView.etsCalendarButton.visibility = View.VISIBLE

                val view = ViewEtsRegisteredBinding.inflate(layoutInflater)

                view.userNameTextView.text = if (courseName.length <= 1) {
                    "Clave: $key"
                } else {
                    courseName.toProperCase()
                }

                view.resultTextView.text =
                    if (result[0].toInt() !in 48..57 && result.length <= 1) {
                        "Sin calificar"
                    } else {
                        result
                    }

                if(result.toIntOrNull() != null && activity != null){
                    if(result.toInt() < 6){
                        view.resultTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorHighlight))
                    }
                }

                rootView.registeredContainer.addView(view.root)
            }
        }

        @JavascriptInterface
        fun showVoucherFab() {
            activity?.runOnUiThread {
                if (!isFabVisible) {
                    (activity as SAESActivity?)?.showFab(
                        R.drawable.ic_file_download_white_24dp,
                        {
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
                && rootView.etsResultsLayout.visibility != View.VISIBLE
                && rootView.layoutInscribirETS.visibility != View.VISIBLE) {
                (activity as SAESActivity).showEmptyText("No tienes examenes disponibles")
            }
        }

        @JavascriptInterface
        fun reload() {
            registerWebView.reload()
        }
    }
}