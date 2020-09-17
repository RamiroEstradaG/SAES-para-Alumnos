package ziox.ramiro.saes.dialogs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_add_evento_calendario_trabajo.view.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.databases.Evento
import ziox.ramiro.saes.databases.addEvento
import ziox.ramiro.saes.utils.format
import ziox.ramiro.saes.databases.updateEvento
import java.util.*


class AddEventoCalendarioTrabajoDialog (private val codigo : String, private val edit : Evento? = null) : DialogFragment() {
    private val eventDate = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Dialog_NoActionBar_MinWidth)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.dialog_add_evento_calendario_trabajo, container, false)

        val tipoData = context!!.resources.getStringArray(R.array.tipo_eventos)

        if((activity as SAESActivity).supportShortcutPin()){
            rootView.buttonAddShortcut.setOnClickListener {
                (activity as? SAESActivity)?.initShortcut()
            }
        }else{
            rootView.buttonAddShortcut.visibility = View.GONE
            rootView.addEventoDivider.visibility = View.GONE
        }

        rootView.eventTipoInput.setOptions(tipoData)

        if(edit != null){
            rootView.eventNameInput?.editText?.text = Editable.Factory.getInstance().newEditable(edit.titulo)
            rootView.eventMateriaInput?.editText?.text = Editable.Factory.getInstance().newEditable(edit.materia)
            rootView.eventTipoInput.setSelection(tipoData.indexOf(edit.tipo))
            rootView.eventInfoInput?.editText?.text = Editable.Factory.getInstance().newEditable(edit.info)

        }

        rootView.eventTipoInput.isSelectOnInitEnable = true

        rootView.addEventAccept.setOnClickListener {
            rootView.eventNameInput.error = null
            if(rootView.eventNameInput?.editText?.text?.isBlank() == true){
                rootView.eventNameInput.error = "El nombre debe contener más de 1 letra."
                return@setOnClickListener
            }

            if(edit == null){
                addEvento(codigo, Evento(
                    eventDate.timeInMillis,
                    rootView.eventNameInput?.editText?.text.toString(),
                    rootView.eventMateriaInput?.editText?.text.toString(),
                    rootView.eventTipoInput.selectedItem?.text.toString(),
                    rootView.eventInfoInput?.editText?.text.toString(),
                    true,
                    codigo
                )
                )
            }else{
                updateEvento(codigo, Evento(
                    eventDate.timeInMillis,
                    rootView.eventNameInput?.editText?.text.toString(),
                    rootView.eventMateriaInput?.editText?.text.toString(),
                    rootView.eventTipoInput.selectedItem?.text.toString(),
                    rootView.eventInfoInput?.editText?.text.toString(),
                    true,
                    codigo,
                    edit.id
                )
                )
            }.addOnSuccessListener {
                this.dismiss()
            }
        }

        activity?.runOnUiThread {
            if(edit == null){
                rootView.addEventAccept.text = "Crear"
                rootView.dialogAddEventoTitle.text = "Nuevo elemento"
            }else{
                rootView.addEventAccept.text = "Editar"
                rootView.dialogAddEventoTitle.text = "Editar elemento"
                eventDate.timeInMillis = edit.dia
            }

            rootView.eventDateInput?.editText?.text = Editable.Factory().newEditable(eventDate.format())
        }

        rootView.eventDateInput.isEndIconVisible = true
        rootView.eventDateInput.setEndIconOnClickListener {
            DatePickerDialog(activity!!, R.style.DatePickerTheme, { _, year, month, day ->
                TimePickerDialog(activity, R.style.TimePickerTheme, { _, hour, minute ->
                    eventDate.set(year, month, day, hour, minute)
                    activity?.runOnUiThread {
                        rootView.eventDateInput?.editText?.text = Editable.Factory().newEditable(eventDate.format())
                    }
                }, eventDate.get(Calendar.HOUR_OF_DAY), eventDate.get(Calendar.MINUTE), true).show()
            }, eventDate.get(Calendar.YEAR), eventDate.get(Calendar.MONTH), eventDate.get(Calendar.DAY_OF_MONTH)).show()
        }

        rootView.addEventCancel.setOnClickListener {
            this.dismiss()
        }

        return rootView
    }


}