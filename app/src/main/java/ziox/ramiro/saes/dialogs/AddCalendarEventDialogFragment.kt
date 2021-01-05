package ziox.ramiro.saes.dialogs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.databases.CalendarEvent
import ziox.ramiro.saes.databases.addEvent
import ziox.ramiro.saes.utils.format
import ziox.ramiro.saes.databases.updateEvent
import ziox.ramiro.saes.databinding.DialogFragmentAddEventoCalendarioTrabajoBinding
import java.util.*


class AddCalendarEventDialogFragment (private val code : String, private val edit : CalendarEvent? = null) : DialogFragment() {
    private val eventDate = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Dialog_NoActionBar_MinWidth)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = DialogFragmentAddEventoCalendarioTrabajoBinding.inflate(inflater, container, false)

        val typeData = context!!.resources.getStringArray(R.array.tipo_eventos)

        if((activity as SAESActivity).supportShortcutPin()){
            rootView.addShortcutButton.setOnClickListener {
                (activity as? SAESActivity)?.initShortcut()
            }
        }else{
            rootView.addShortcutButton.visibility = View.GONE
            rootView.addEventDivider.visibility = View.GONE
        }

        rootView.eventTypeInput.setOptions(typeData)

        if(edit != null){
            rootView.eventTitleInput.editText?.text = Editable.Factory.getInstance().newEditable(edit.title)
            rootView.eventCourseNameInput.editText?.text = Editable.Factory.getInstance().newEditable(edit.courseName)
            rootView.eventTypeInput.setSelection(typeData.indexOf(edit.type))
            rootView.eventInfoInput.editText?.text = Editable.Factory.getInstance().newEditable(edit.info)

        }

        rootView.eventTypeInput.isSelectOnInitEnable = true

        rootView.acceptButton.setOnClickListener {
            rootView.eventTitleInput.error = null
            if(rootView.eventTitleInput.editText?.text?.isBlank() == true){
                rootView.eventTitleInput.error = "El nombre debe contener más de 1 letra."
                return@setOnClickListener
            }

            if(edit == null){
                addEvent(code, CalendarEvent(
                    eventDate.timeInMillis,
                    rootView.eventTitleInput.editText?.text.toString(),
                    rootView.eventCourseNameInput.editText?.text.toString(),
                    rootView.eventTypeInput.selectedItem?.text.toString(),
                    rootView.eventInfoInput.editText?.text.toString(),
                    true,
                    code
                )
                )
            }else{
                updateEvent(code, CalendarEvent(
                    eventDate.timeInMillis,
                    rootView.eventTitleInput.editText?.text.toString(),
                    rootView.eventCourseNameInput.editText?.text.toString(),
                    rootView.eventTypeInput.selectedItem?.text.toString(),
                    rootView.eventInfoInput.editText?.text.toString(),
                    true,
                    code,
                    edit.id
                )
                )
            }.addOnSuccessListener {
                this.dismiss()
            }
        }

        activity?.runOnUiThread {
            if(edit == null){
                rootView.acceptButton.text = "Crear"
                rootView.dialogTitle.text = "Nuevo elemento"
            }else{
                rootView.acceptButton.text = "Editar"
                rootView.dialogTitle.text = "Editar elemento"
                eventDate.timeInMillis = edit.date
            }

            rootView.eventDateInput.editText?.text = Editable.Factory().newEditable(eventDate.format())
        }

        rootView.eventDateInput.isEndIconVisible = true
        rootView.eventDateInput.setEndIconOnClickListener {
            DatePickerDialog(requireContext(), R.style.DatePickerTheme, { _, year, month, day ->
                TimePickerDialog(activity, R.style.TimePickerTheme, { _, hour, minute ->
                    eventDate.set(year, month, day, hour, minute)
                    activity?.runOnUiThread {
                        rootView.eventDateInput.editText?.text = Editable.Factory().newEditable(eventDate.format())
                    }
                }, eventDate.get(Calendar.HOUR_OF_DAY), eventDate.get(Calendar.MINUTE), true).show()
            }, eventDate.get(Calendar.YEAR), eventDate.get(Calendar.MONTH), eventDate.get(Calendar.DAY_OF_MONTH)).show()
        }

        rootView.cancelButton.setOnClickListener {
            this.dismiss()
        }

        return rootView.root
    }


}