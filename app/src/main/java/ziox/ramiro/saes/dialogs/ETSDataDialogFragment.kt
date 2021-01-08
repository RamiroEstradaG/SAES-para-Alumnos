package ziox.ramiro.saes.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ziox.ramiro.saes.databinding.DialogFragmentEtsDataBinding
import ziox.ramiro.saes.views.calendar_view.CalendarView

/**
 * Creado por Ramiro el 1/31/2019 a las 3:48 PM para SAESv2.
 */
class ETSDataDialogFragment : DialogFragment() {
    companion object {
        var data : CalendarView.EventData? = null
        fun newInstance(data : CalendarView.EventData) {
            ETSDataDialogFragment.data = data
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
    ): View {
        val rootView = DialogFragmentEtsDataBinding.inflate(inflater, container, false)

        rootView.titleTextView.text = data?.title
        rootView.buildingNameTextView.text = data?.buildingName
        rootView.classroomNameTextView.text = data?.classroomName

        return rootView.root
    }
}