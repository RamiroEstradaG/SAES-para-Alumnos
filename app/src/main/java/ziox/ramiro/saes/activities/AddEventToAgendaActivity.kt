package ziox.ramiro.saes.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ziox.ramiro.saes.R
import ziox.ramiro.saes.databases.CalendarEvent
import ziox.ramiro.saes.databases.addEvent
import ziox.ramiro.saes.databases.enablePersistence
import ziox.ramiro.saes.databases.getAdminCalendar
import ziox.ramiro.saes.databinding.ActivityAddEventToAgendaBinding
import ziox.ramiro.saes.utils.*
import java.util.*

class AddEventToAgendaActivity : AppCompatActivity(), View.OnClickListener{
    private val eventDate = Calendar.getInstance()
    private var code = ""
    private lateinit var binding: ActivityAddEventToAgendaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEventToAgendaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarByTheme(this)
        setSupportActionBar(binding.toolbar)
        enablePersistence(true)
        title = "Agregar evento"
        binding.containerScroll.addBottomInsetPadding()

        val eventTypes = resources.getStringArray(R.array.tipo_eventos)

        getAdminCalendar(this){
            if(it.isEmpty()) return@getAdminCalendar

            binding.selectionCalendar.isSelectOnInitEnable = true
            binding.selectionCalendar.setOptions(it.map { cal->
                cal.name
            }.toTypedArray())
            code = it.first().code
            binding.selectionCalendar.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    code = it[p2].code
                }

            }
        }

        binding.eventTypeInput.setOptions(eventTypes)
        binding.eventTypeInput.isSelectOnInitEnable = true


        binding.eventDateInput.editText?.text = Editable.Factory().newEditable(eventDate.format())

        binding.eventDateInput.isEndIconVisible = true

        binding.eventDateInput.setEndIconOnClickListener (this)
        binding.acceptButton.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.acceptButton -> {
                binding.eventTitleInput.error = null
                if(binding.eventTitleInput.editText?.text?.isBlank() == true){
                    binding.eventTitleInput.error = "El nombre debe contener más de 1 letra."
                    return
                }

                if(code.isBlank()){
                    Toast.makeText(this, "Seleccione una agenda", Toast.LENGTH_SHORT).show()
                }

                addEvent(code,
                    CalendarEvent(
                        eventDate.timeInMillis,
                        binding.eventTitleInput.editText?.text.toString(),
                        binding.eventCourseNameInput.editText?.text.toString(),
                        binding.eventTypeInput.selectedItem?.text.toString(),
                        binding.eventInfoInput.editText?.text.toString(),
                        true,
                        code
                    )
                )

                Toast.makeText(this, "Correcto", Toast.LENGTH_SHORT).show()
                this.finish()
            }
            com.google.android.material.R.layout.design_text_input_end_icon -> {
                DatePickerDialog(this, R.style.DatePickerTheme, { _, year, month, day ->
                    TimePickerDialog(this, R.style.TimePickerTheme, { _, hour, minute ->
                        eventDate.set(year, month, day, hour, minute)
                        runOnUiThread {
                            binding.eventDateInput.editText?.text = Editable.Factory().newEditable(eventDate.format())
                        }
                    }, eventDate.get(Calendar.HOUR_OF_DAY), eventDate.get(Calendar.MINUTE), true).show()
                }, eventDate.get(Calendar.YEAR), eventDate.get(Calendar.MONTH), eventDate.get(Calendar.DAY_OF_MONTH)).show()
            }
        }
    }
}