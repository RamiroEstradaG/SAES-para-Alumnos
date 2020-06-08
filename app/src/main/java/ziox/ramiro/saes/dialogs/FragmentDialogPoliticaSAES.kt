package ziox.ramiro.saes.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_fragment_licencias.view.*
import ziox.ramiro.saes.R

/**
 * Creado por Ramiro el 9/4/2018 a las 9:10 PM para SAES.
 */
class FragmentDialogPoliticaSAES : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Dialog_NoActionBar_MinWidth)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_fragment_licencias, container, false)
        rootView.wv_licencia.loadDataWithBaseURL(null, "La direccion de Administración Escolar (DAE) le informa que los Datos Personales proporcionados por usted son protegidos, incorporados y tratados en el ''Sistema de Administracion Escolar'' (SAES), el cual fue registrado en el Listado de Sistemas de Datos Personales ante el Instituto Federal de Acceso a la Información Pública (IFAI) (www.ifai.org.mx), con fundamento en los Articulos 20 y 21 de la Ley Federal de Transparencia y Acceso a la Información Pública Gubernamental (LFTAIPG), y demás disposiciones aplicables y podrán ser proporcionados a dependencias del Instituto Politécnico Nacional y autoridades competentes, con la finalidad de coadyuvar al ejercicio de las funciones propias de la Institución, además de otra información prevista en la ley. La Unidad Administrativa Responsable del SAES es la DAE, en la cual podrá ejercer los derechos de acceso y corrección. Sita en la ''Unidad Profesional Adolfo López Mateos'', Av. Instituto Politécnico Nacional No. 1936, Col. Zacatenco, México, D.F., CP. 07738. Lo anterior con base al décimo séptimo Lineamiento de Protección de Datos Personales publicados en el Diario Oficial de la Federación (D.O.F. 30 de septiembre de 2005)", "text/html", "utf-8", null)

        return rootView
    }
}