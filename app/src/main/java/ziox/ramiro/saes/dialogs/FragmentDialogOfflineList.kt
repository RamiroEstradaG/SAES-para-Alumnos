package ziox.ramiro.saes.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_offline_list.view.*
import ziox.ramiro.saes.R


/**
 * Creado por Ramiro el 7/21/2018 a las 10:48 PM para SAES.
 */
class FragmentDialogOfflineList : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Dialog_NoActionBar_MinWidth)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_offline_list, container, false)
        val adapter: ArrayAdapter<String> = ArrayAdapter(activity!!, R.layout.view_spinner_item)
        rootView.offlineList.adapter = adapter
        adapter.add("Kárdex")
        adapter.add("Rendimiento")
        adapter.add("Horario de clase")
        adapter.add("Calificaciones")
        adapter.add("Cita de reinscripción")
        adapter.add("ETS")
        adapter.add("Agenda escolar")
        adapter.add("Generador de horario")
        return rootView
    }
}