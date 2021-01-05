package ziox.ramiro.saes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import ziox.ramiro.saes.databinding.FragmentPersonalMedicalDataBinding
import ziox.ramiro.saes.utils.addBottomInsetPadding
import ziox.ramiro.saes.utils.createWebView
import ziox.ramiro.saes.utils.getUrl
import java.util.*

/**
 * Creado por Ramiro el 12/13/2018 a las 2:22 PM para SAESv2.
 */
class PersonalMedicalDataFragment : Fragment() {
    lateinit var rootView: FragmentPersonalMedicalDataBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = FragmentPersonalMedicalDataBinding.inflate(inflater, container, false)
        rootView.parent.addBottomInsetPadding()
        val medicDataWebView = createWebView(activity, object : WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.loadUrl("javascript: " +
                        "function getText(id){" +
                        "   return document.getElementById(id).innerText;" +
                        "}" +
                        "" +
                        "window.JSI.setGeneralData(getText(\"ctl00_mainCopy_LblPesoMod\")," +
                        "getText(\"ctl00_mainCopy_LblEstaturaMod\")," +
                        "getText(\"ctl00_mainCopy_LblSangreMod\"));" +
                        "" +
                        "window.JSI.setMedicalAffiliation(getText(\"ctl00_mainCopy_LblInstitucion\")," +
                        "getText(\"ctl00_mainCopy_LblNumSS\")," +
                        "getText(\"ctl00_mainCopy_LblFecha_In\")," +
                        "getText(\"ctl00_mainCopy_LblFecha_Out\"));" +
                        "" +
                        "window.JSI.setIntegrityData(getText(\"ctl00_mainCopy_LblIsEnfMod\")+\", \"+getText(\"ctl00_mainCopy_LblDescEnfMod\")," +
                        "getText(\"ctl00_mainCopy_LblIsProbFisMod\")+\", \"+getText(\"ctl00_mainCopy_LblDescProbMod\")," +
                        "getText(\"ctl00_mainCopy_LblIsTatuadoMod\"), getText(\"ctl00_mainCopy_LblIsPlanoMod\"));")
            }
        }, null)

        medicDataWebView.addJavascriptInterface(MedicalDataJSI(), "JSI")

        medicDataWebView.loadUrl(getUrl(activity)+"Alumnos/info_alumnos/DatosAlumnosMedicos.aspx")

        return rootView.root
    }

    inner class MedicalDataJSI{
        @JavascriptInterface
        fun setGeneralData(weight : String, height : String, bloodType : String){
            activity?.runOnUiThread {
                rootView.weigthTextView.text = if(weight.toFloat() > 0f){
                    "$weight kg"
                }else{
                    "Sin datos"
                }
                rootView.heightTextView.text = if(height.toFloat() > 0f){
                    "${height}m"
                }else{
                    "Sin datos"
                }
                rootView.bloodTypeTextView.text = bloodType
            }
        }

        @JavascriptInterface
        fun setMedicalAffiliation(institution: String, socialSecurityNumber : String, registerDate : String, deregisterDate : String){
            activity?.runOnUiThread {
                rootView.institutionTextView.text = institution
                rootView.socialSecurityNumberTextView.text = if(socialSecurityNumber.length < 2){
                    "Sin datos"
                }else{
                    socialSecurityNumber
                }
                rootView.registerDateTextView.text = if(registerDate.length < 2){
                    "Sin datos"
                }else{
                    registerDate
                }
                rootView.deregisterDateTextView.text = if(registerDate.length < 2){
                    "Sin datos"
                }else{
                    deregisterDate
                }
            }
        }

        @JavascriptInterface
        fun setIntegrityData(diseases : String, physicalProblems : String, tattoos : String, flatfoot : String){
            activity?.runOnUiThread {
                rootView.diseasesTextView.text = diseases.toLowerCase(Locale.ROOT).capitalize(Locale.ROOT)
                rootView.physicalProblemsTextView.text = physicalProblems.toLowerCase(Locale.ROOT).capitalize(Locale.ROOT)
                rootView.tattoosTextView.text = tattoos.toLowerCase(Locale.ROOT).capitalize(Locale.ROOT)
                rootView.flatfootTextView.text = flatfoot.toLowerCase(Locale.ROOT).capitalize(Locale.ROOT)
            }
        }
    }
}