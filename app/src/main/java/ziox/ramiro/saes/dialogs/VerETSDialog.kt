package ziox.ramiro.saes.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_ver_ets.view.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.views.scheduleview.ScheduleView

/**
 * Creado por Ramiro el 1/31/2019 a las 3:48 PM para SAESv2.
 */
class VerETSDialog : DialogFragment() {
    companion object {
        var data : ScheduleView.EventData? = null
        fun newInstance(data : ScheduleView.EventData) {
            VerETSDialog.data = data
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Dialog_NoActionBar_MinWidth)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = layoutInflater.inflate(R.layout.dialog_ver_ets, container, false)


        rootView?.dialog_saes_ets_titulo?.text = data?.title
        rootView?.dialog_saes_ets_edificio?.text = data?.edificio
        rootView?.dialog_saes_ets_salon?.text = data?.salon

        return rootView
    }
}