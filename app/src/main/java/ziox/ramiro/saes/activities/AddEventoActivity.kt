package ziox.ramiro.saes.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_add_evento.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.utils.*
import java.util.*

class AddEventoActivity : AppCompatActivity(){
    private val eventDate = Calendar.getInstance()
    private var codigo = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_evento)
        setStatusBarByTheme(this)
        setSupportActionBar(toolbar4)
        enablePersistance(true)
        title = "Agregar evento"
        scrollAddEvento.addBottomInsetPadding()

        val tipoData = resources.getStringArray(R.array.tipo_eventos)

        getAdminCalendar(this){
            if(it.isEmpty()) return@getAdminCalendar

            selectionCalendario.isSelectOnInitEnable = true
            selectionCalendario.setOptions(it.map { cal->
                cal.name
            }.toTypedArray())
            codigo = it.first().codigo
            selectionCalendario.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    codigo = it[p2].codigo
                }

            }
        }

        eventTipoInput.setOptions(tipoData)
        eventTipoInput.isSelectOnInitEnable = true

        addEventAccept.setOnClickListener {
            eventNameInput.error = null
            if(eventNameInput?.editText?.text?.isBlank() == true){
                eventNameInput.error = "El nombre debe contener más de 1 letra."
                return@setOnClickListener
            }

            if(codigo.isBlank()){
                Toast.makeText(this, "Seleccione un calendario", Toast.LENGTH_SHORT).show()
            }

            addEvento(codigo, Evento(
                eventDate.timeInMillis,
                eventNameInput?.editText?.text.toString(),
                eventMateriaInput?.editText?.text.toString(),
                eventTipoInput.selectedItem?.text.toString(),
                eventInfoInput?.editText?.text.toString(),
                true,
                codigo
            ))

            Toast.makeText(this, "Correcto", Toast.LENGTH_SHORT).show()
            this.finish()
        }

        eventDateInput?.editText?.text = Editable.Factory().newEditable(eventDate.format())

        eventDateInput.isEndIconVisible = true
        eventDateInput.setEndIconOnClickListener {
            DatePickerDialog(this, R.style.DatePickerTheme, { _, year, month, day ->
                TimePickerDialog(this, R.style.TimePickerTheme, { _, hour, minute ->
                    eventDate.set(year, month, day, hour, minute)
                    runOnUiThread {
                        eventDateInput?.editText?.text = Editable.Factory().newEditable(eventDate.format())
                    }
                }, eventDate.get(Calendar.HOUR_OF_DAY), eventDate.get(Calendar.MINUTE), true).show()
            }, eventDate.get(Calendar.YEAR), eventDate.get(Calendar.MONTH), eventDate.get(Calendar.DAY_OF_MONTH)).show()
        }
    }
}