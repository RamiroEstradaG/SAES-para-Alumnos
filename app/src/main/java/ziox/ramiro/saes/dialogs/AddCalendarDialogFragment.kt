package ziox.ramiro.saes.dialogs

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ziox.ramiro.saes.R
import ziox.ramiro.saes.databases.addCalendar
import ziox.ramiro.saes.databases.addCalendarToUser
import ziox.ramiro.saes.databinding.DialogFragmentAddCalendarBinding
import ziox.ramiro.saes.utils.generateRandomString

class AddCalendarDialogFragment : DialogFragment() {
    private lateinit var onSuccessListener: () -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Dialog_NoActionBar_MinWidth)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = DialogFragmentAddCalendarBinding.inflate(inflater, container, false)

        rootView.acceptButton.setOnClickListener {
            rootView.calendarTitleInput.error = null
            if(rootView.calendarTitleInput.editText?.text.toString().isNotEmpty()){
                val alertPermission = AlertDialog.Builder(activity, R.style.DialogAlert)
                alertPermission.setTitle("Aviso")
                alertPermission.setMessage("Al presionar 'Aceptar' otorgas permiso a la aplicación para almacenar en la nube:\n\n• Número de boleta.\n• Recordatorios.")
                alertPermission.setPositiveButton("Aceptar"){ _, _ ->
                    val id = generateRandomString(10)
                    addCalendar(activity ,rootView.calendarTitleInput.editText?.text.toString(), id, !rootView.grupCalendarCheck.isChecked).addOnSuccessListener {
                        addCalendarToUser(context, id).addOnSuccessListener {
                            this.onSuccessListener()
                            this.dismiss()
                        }
                    }
                }
                alertPermission.setNegativeButton("Cancelar", null)
                alertPermission.show()
            }else{
                rootView.calendarTitleInput.error = "El título no puede ser vacío"
            }
        }

        rootView.cancelButton.setOnClickListener {
            this.dismiss()
        }

        rootView.joinCalendarButton.setOnClickListener {
            rootView.joinCalendarInput.error = null
            if(rootView.joinCalendarInput.editText?.text?.length == 8){
                addCalendarToUser(context, rootView.joinCalendarInput.editText?.text.toString()).addOnSuccessListener {
                    this.onSuccessListener()
                    this.dismiss()
                }
            }else{
                rootView.joinCalendarInput.error = "Ingresa un código válido de 8 caracteres"
            }
        }

        return rootView.root
    }

    fun setOnSuccessListener(lambda: () -> Unit){
        this.onSuccessListener = lambda
    }
}