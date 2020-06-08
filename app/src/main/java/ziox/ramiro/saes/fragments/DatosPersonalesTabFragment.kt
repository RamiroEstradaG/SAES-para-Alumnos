package ziox.ramiro.saes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.tab_fragment_datos_personales.view.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.utils.createWebView
import ziox.ramiro.saes.utils.getUrl
import ziox.ramiro.saes.utils.addBottomInsetPadding
import ziox.ramiro.saes.utils.toProperCase
import java.util.*

/**
 * Creado por Ramiro el 12/13/2018 a las 2:21 PM para SAESv2.
 */
class DatosPersonalesTabFragment : Fragment() {
    lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.tab_fragment_datos_personales, container, false)
        rootView.datosPersonalesParent.addBottomInsetPadding()

        val datosPersonalesWebView = createWebView(activity, object : WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.loadUrl("javascript: " +
                        "function getText(id){" +
                        "   return document.getElementById(id).innerText;" +
                        "}" +
                        "" +
                        "window.JSI.setNacimiento(getText(\"ctl00_mainCopy_TabContainer1_TabPanel1_Lbl_FecNac\")," +
                        "getText(\"ctl00_mainCopy_TabContainer1_TabPanel1_Lbl_Nacionalidad\")," +
                        "getText(\"ctl00_mainCopy_TabContainer1_TabPanel1_Lbl_EntNac\"));" +
                        "" +
                        "var directionCompleta = getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_Calle\")+" +
                        "\" #\"+getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_NumExt\");" +
                        "if(getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_NumInt\") != \"0\" && getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_NumInt\") != \"\") directionCompleta += \" No. Int. #\"+getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_NumInt\");" +
                        "directionCompleta += \" Col. \"+getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_Colonia\")+\". C. P. \"+getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_CP\");" +
                        "window.JSI.setDireccion(directionCompleta, getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_DelMpo\")+\", \"+getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_Estado\")," +
                        "getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_Tel\")," +
                        "getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_Movil\")," +
                        "getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_TelOficina\")," +
                        "getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_eMail\"));" +
                        "" +
                        "window.JSI.setEscolaridad(getText(\"ctl00_mainCopy_TabContainer1_Tab_Escolaridad_Lbl_EscProc\")," +
                        "getText(\"ctl00_mainCopy_TabContainer1_Tab_Escolaridad_Lbl_PromSec\")," +
                        "getText(\"ctl00_mainCopy_TabContainer1_Tab_Escolaridad_Lbl_PromNMS\"));" +
                        "" +
                        "window.JSI.setTutores(getText(\"ctl00_mainCopy_TabContainer1_Tab_Tutor_Lbl_NomTut\")+\" - \"+getText(\"ctl00_mainCopy_TabContainer1_Tab_Tutor_Lbl_RFCTut\")," +
                        "getText(\"ctl00_mainCopy_TabContainer1_Tab_Tutor_Lbl_Padre\")," +
                        "getText(\"ctl00_mainCopy_TabContainer1_Tab_Tutor_Lbl_Madre\"));")
            }
        }, null)

        datosPersonalesWebView.addJavascriptInterface(JsiPersonales(), "JSI")

        datosPersonalesWebView.loadUrl(getUrl(activity)+"Alumnos/info_alumnos/Datos_Alumno.aspx")

        return rootView
    }

    inner class JsiPersonales{

        @JavascriptInterface
        fun setNacimiento(nacimiento: String, nacionalidad : String, entidad : String){
            activity?.runOnUiThread {
                rootView.labelFechaNacimiento.text = if(nacimiento.length > 3){
                    nacimiento
                }else{
                    "Sin datos"
                }
                rootView.labelNacionalidad.text = if(nacionalidad.length > 3){
                    nacionalidad.toProperCase()
                }else{
                    "Sin datos"
                }
                rootView.labelEntidadNacimiento.text = if(entidad.length > 3){
                    entidad.toProperCase()
                }else{
                    "Sin datos"
                }
            }
        }

        @JavascriptInterface
        fun setDireccion(direccion : String, localidad : String, tel1 : String,tel2 : String,tel3 : String,email : String){
            activity?.runOnUiThread {
                rootView.labelDireccion.text = if(direccion.length > 15){
                    direccion.toProperCase()
                }else{
                    "Sin datos suficientes"
                }
                rootView.labelMunicipio.text = if(localidad.length > 3){
                    localidad.toProperCase()
                }else{
                    "Sin datos"
                }
                rootView.labelTelefono.text = if(tel1.length > 3){
                    tel1
                }else{
                    "Sin teléfono"
                }
                rootView.labelMovil.text = if(tel2.length > 3){
                    tel2
                }else{
                    "Sin teléfono"
                }
                rootView.labelTelOficina.text = if(tel3.length > 3){
                    tel3
                }else{
                    "Sin teléfono"
                }
                rootView.labelEmail.text = if(email.length > 3){
                    email
                }else{
                    "Sin e-mail"
                }
            }
        }

        @JavascriptInterface
        fun setEscolaridad(escuela : String,promSecu : String,promPrepa : String){
            activity?.runOnUiThread {
                rootView.labelEscuelaProcedencia.text = escuela.toLowerCase(Locale.ROOT).capitalize()
                rootView.labelPromedioSecu.text = if(promSecu.toFloatOrNull()?:0f < 5f){
                    "Sin promedio"
                }else{
                    promSecu
                }
                rootView.labelPromedioPrepa.text = if(promPrepa.toFloatOrNull()?:0f < 5f){
                    "Sin promedio"
                }else{
                    promPrepa
                }
            }
        }

        @JavascriptInterface
        fun setTutores(tutor : String,padre : String,madre : String){
            activity?.runOnUiThread {
                rootView.labelTutor.text = if(tutor[0] != ' '){
                    tutor
                }else{
                    "Sin tutor"
                }
                rootView.labelPadre.text = if(padre.length > 3){
                    padre.toProperCase()
                }else{
                    "Sin datos"
                }
                rootView.labelMadre.text = if(madre.length > 3){
                    madre.toProperCase()
                }else{
                    "Sin datos"
                }
            }
        }
    }
}