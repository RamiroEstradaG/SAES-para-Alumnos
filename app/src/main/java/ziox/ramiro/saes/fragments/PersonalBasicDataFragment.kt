package ziox.ramiro.saes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import ziox.ramiro.saes.databinding.FragmentPersonalBasicDataBinding
import ziox.ramiro.saes.utils.addBottomInsetPadding
import ziox.ramiro.saes.utils.createWebView
import ziox.ramiro.saes.utils.getUrl
import ziox.ramiro.saes.utils.toProperCase
import java.util.*

/**
 * Creado por Ramiro el 12/13/2018 a las 2:21 PM para SAESv2.
 */
class PersonalBasicDataFragment : Fragment() {
    lateinit var rootView: FragmentPersonalBasicDataBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = FragmentPersonalBasicDataBinding.inflate(inflater, container, false)
        rootView.datosPersonalesParent.addBottomInsetPadding()

        val datosPersonalesWebView = createWebView(activity, object : WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.loadUrl("javascript: " +
                        "function getText(id){" +
                        "   return document.getElementById(id).innerText;" +
                        "}" +
                        "" +
                        "window.JSI.setBirthData(getText(\"ctl00_mainCopy_TabContainer1_TabPanel1_Lbl_FecNac\")," +
                        "getText(\"ctl00_mainCopy_TabContainer1_TabPanel1_Lbl_Nacionalidad\")," +
                        "getText(\"ctl00_mainCopy_TabContainer1_TabPanel1_Lbl_EntNac\"));" +
                        "" +
                        "var directionCompleta = getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_Calle\")+" +
                        "\" #\"+getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_NumExt\");" +
                        "if(getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_NumInt\") != \"0\" && getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_NumInt\") != \"\") directionCompleta += \" No. Int. #\"+getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_NumInt\");" +
                        "directionCompleta += \". \"+getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_Colonia\")+\". C.P. \"+getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_CP\");" +
                        "window.JSI.setAddressData(directionCompleta, getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_DelMpo\")+\", \"+getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_Estado\")," +
                        "getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_Tel\")," +
                        "getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_Movil\")," +
                        "getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_TelOficina\")," +
                        "getText(\"ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_eMail\"));" +
                        "" +
                        "window.JSI.setScholarshipData(getText(\"ctl00_mainCopy_TabContainer1_Tab_Escolaridad_Lbl_EscProc\")," +
                        "getText(\"ctl00_mainCopy_TabContainer1_Tab_Escolaridad_Lbl_PromSec\")," +
                        "getText(\"ctl00_mainCopy_TabContainer1_Tab_Escolaridad_Lbl_PromNMS\"));" +
                        "" +
                        "window.JSI.setGuardianData(getText(\"ctl00_mainCopy_TabContainer1_Tab_Tutor_Lbl_NomTut\")+\" - \"+getText(\"ctl00_mainCopy_TabContainer1_Tab_Tutor_Lbl_RFCTut\")," +
                        "getText(\"ctl00_mainCopy_TabContainer1_Tab_Tutor_Lbl_Padre\")," +
                        "getText(\"ctl00_mainCopy_TabContainer1_Tab_Tutor_Lbl_Madre\"));")
            }
        }, null)

        datosPersonalesWebView.addJavascriptInterface(PersonalBasicDataJSI(), "JSI")

        datosPersonalesWebView.loadUrl(getUrl(activity)+"Alumnos/info_alumnos/Datos_Alumno.aspx")

        return rootView.root
    }

    inner class PersonalBasicDataJSI{

        @JavascriptInterface
        fun setBirthData(birthday: String, nationality : String, place : String){
            activity?.runOnUiThread {
                rootView.birthdayTextView.text = if(birthday.length > 3){
                    birthday
                }else{
                    "Sin datos"
                }
                rootView.nationalityTextView.text = if(nationality.length > 3){
                    nationality.toProperCase()
                }else{
                    "Sin datos"
                }
                rootView.placeTextView.text = if(place.length > 3){
                    place.toProperCase()
                }else{
                    "Sin datos"
                }
            }
        }

        @JavascriptInterface
        fun setAddressData(address : String, city : String, phone : String, mobilePhone : String, officePhone : String, email : String){
            activity?.runOnUiThread {
                rootView.addressTextView.text = if(address.length > 15){
                    address.toProperCase()
                }else{
                    "Sin datos suficientes"
                }
                rootView.cityTextView.text = if(city.length > 3){
                    city.toProperCase()
                }else{
                    "Sin datos"
                }
                rootView.phoneTextView.text = if(phone.length > 3){
                    phone
                }else{
                    "Sin teléfono"
                }
                rootView.mobilePhoneTextView.text = if(mobilePhone.length > 3){
                    mobilePhone
                }else{
                    "Sin teléfono"
                }
                rootView.officePhoneTextView.text = if(officePhone.length > 3){
                    officePhone
                }else{
                    "Sin teléfono"
                }
                rootView.emailTextView.text = if(email.length > 3){
                    email
                }else{
                    "Sin e-mail"
                }
            }
        }

        @JavascriptInterface
        fun setScholarshipData(highSchoolName : String, middleSchoolFinalScore : String, highSchoolFinalScore : String){
            activity?.runOnUiThread {
                rootView.highSchoolTextView.text = highSchoolName.toLowerCase(Locale.ROOT).capitalize()
                rootView.middleSchoolScoreTextView.text = if(middleSchoolFinalScore.toFloatOrNull()?:0f < 5f){
                    "Sin promedio"
                }else{
                    middleSchoolFinalScore
                }
                rootView.highSchoolScoreTextView.text = if(highSchoolFinalScore.toFloatOrNull()?:0f < 5f){
                    "Sin promedio"
                }else{
                    highSchoolFinalScore
                }
            }
        }

        @JavascriptInterface
        fun setGuardianData(guardianName : String, fatherName : String, motherName : String){
            activity?.runOnUiThread {
                rootView.guardianNameTextView.text = if(guardianName[0] != ' '){
                    guardianName
                }else{
                    "Sin tutor"
                }
                rootView.fatherTextView.text = if(fatherName.length > 3){
                    fatherName.toProperCase()
                }else{
                    "Sin datos"
                }
                rootView.motherTextView.text = if(motherName.length > 3){
                    motherName.toProperCase()
                }else{
                    "Sin datos"
                }
            }
        }
    }
}