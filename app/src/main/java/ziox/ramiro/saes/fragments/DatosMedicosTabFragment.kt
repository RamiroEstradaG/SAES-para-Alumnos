package ziox.ramiro.saes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.tab_fragment_datos_medicos.view.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.utils.createWebView
import ziox.ramiro.saes.utils.getUrl
import ziox.ramiro.saes.utils.addBottomInsetPadding

/**
 * Creado por Ramiro el 12/13/2018 a las 2:22 PM para SAESv2.
 */
class DatosMedicosTabFragment : Fragment() {
    lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.tab_fragment_datos_medicos, container, false)
        rootView.datosMedicosParent.addBottomInsetPadding()
        val datosMedicosWebView = createWebView(activity, object : WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.loadUrl("javascript: " +
                        "function getText(id){" +
                        "   return document.getElementById(id).innerText;" +
                        "}" +
                        "" +
                        "window.JSI.setGenerales(getText(\"ctl00_mainCopy_LblPesoMod\")," +
                        "getText(\"ctl00_mainCopy_LblEstaturaMod\")," +
                        "getText(\"ctl00_mainCopy_LblSangreMod\"));" +
                        "" +
                        "window.JSI.setAfiliacion(getText(\"ctl00_mainCopy_LblInstitucion\")," +
                        "getText(\"ctl00_mainCopy_LblNumSS\")," +
                        "getText(\"ctl00_mainCopy_LblFecha_In\")," +
                        "getText(\"ctl00_mainCopy_LblFecha_Out\"));" +
                        "" +
                        "window.JSI.setIntegridad(getText(\"ctl00_mainCopy_LblIsEnfMod\")+\", \"+getText(\"ctl00_mainCopy_LblDescEnfMod\")," +
                        "getText(\"ctl00_mainCopy_LblIsProbFisMod\")+\", \"+getText(\"ctl00_mainCopy_LblDescProbMod\")," +
                        "getText(\"ctl00_mainCopy_LblIsTatuadoMod\"), getText(\"ctl00_mainCopy_LblIsPlanoMod\"));")
            }
        }, null)

        datosMedicosWebView.addJavascriptInterface(JsiMedicos(), "JSI")

        datosMedicosWebView.loadUrl(getUrl(activity)+"Alumnos/info_alumnos/DatosAlumnosMedicos.aspx")

        return rootView
    }

    inner class JsiMedicos{

        @JavascriptInterface
        fun setGenerales(peso : String, estatura : String, sangre : String){
            activity?.runOnUiThread {
                rootView.labelPeso.text = if(peso.toFloat() > 0f){
                    "$peso kg"
                }else{
                    "Sin datos"
                }
                rootView.labelEstatura.text = if(estatura.toFloat() > 0f){
                    "${estatura}m"
                }else{
                    "Sin datos"
                }
                rootView.labelSangre.text = sangre
            }
        }

        @JavascriptInterface
        fun setAfiliacion(inst: String, noSeguro : String, alta : String, baja : String){
            activity?.runOnUiThread {
                rootView.labelInstitucion.text = inst
                rootView.labelNoSeguroSocial.text = if(noSeguro.length < 2){
                    "Sin datos"
                }else{
                    noSeguro
                }
                rootView.labelFechaAlta.text = if(alta.length < 2){
                    "Sin datos"
                }else{
                    alta
                }
                rootView.labelFechaBaja.text = if(alta.length < 2){
                    "Sin datos"
                }else{
                    baja
                }
            }
        }

        @JavascriptInterface
        fun setIntegridad(enfermedades : String,problemas : String,tatuajes : String, piePlano : String){
            activity?.runOnUiThread {
                rootView.labelPadecimientoEnfermedades.text = enfermedades.toLowerCase().capitalize()
                rootView.labelProblemasFisicos.text = problemas.toLowerCase().capitalize()
                rootView.labelTatuado.text = tatuajes.toLowerCase().capitalize()
                rootView.labelPiePlano.text = piePlano.toLowerCase().capitalize()
            }
        }
    }
}