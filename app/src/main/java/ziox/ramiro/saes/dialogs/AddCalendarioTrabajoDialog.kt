package ziox.ramiro.saes.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_add_calendario_trabajo.view.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.utils.addCalendario
import ziox.ramiro.saes.utils.addCalendarioToUser
import ziox.ramiro.saes.utils.generateRandomString

class AddCalendarioTrabajoDialog : DialogFragment() {
    private lateinit var onSuccessListener: () -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Dialog_NoActionBar_MinWidth)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.dialog_add_calendario_trabajo, container, false)

        rootView.addCalendarioAccept.setOnClickListener {
            rootView.addCalendarioInput.error = null
            if(rootView.addCalendarioInput.editText?.text.toString().isNotEmpty()){
                val id = generateRandomString(8)
                addCalendario(activity ,rootView.addCalendarioInput.editText?.text.toString(), id, !rootView.addCalendarioCheck.isChecked).addOnSuccessListener {
                    addCalendarioToUser(context, id).addOnSuccessListener {
                        this.onSuccessListener()
                        this.dismiss()
                    }
                }
            }else{
                rootView.addCalendarioInput.error = "El título no puede ser vacío"
            }
        }

        rootView.addCalendarioCancel.setOnClickListener {
            this.dismiss()
        }

        rootView.joinCalendarioButton.setOnClickListener {
            rootView.joinCalendarioInput.error = null
            if(rootView.joinCalendarioInput?.editText?.text?.length == 8){
                addCalendarioToUser(context, rootView.joinCalendarioInput?.editText?.text.toString()).addOnSuccessListener {
                    this.onSuccessListener()
                    this.dismiss()
                }
            }else{
                rootView.joinCalendarioInput.error = "Ingresa un código válido de 8 caracteres"
            }
        }

        return rootView
    }

    fun setOnSuccessListener(lambda: () -> Unit){
        this.onSuccessListener = lambda
    }
}