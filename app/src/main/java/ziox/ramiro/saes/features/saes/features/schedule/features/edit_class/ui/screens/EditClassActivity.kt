package ziox.ramiro.saes.features.saes.features.schedule.features.edit_class.ui.screens

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import ziox.ramiro.saes.data.models.ValidatorState
import ziox.ramiro.saes.data.models.validate
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.features.agenda.ui.screens.SelectableOptions
import ziox.ramiro.saes.features.saes.features.agenda.ui.screens.showHourPickerDialog
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.CustomClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ScheduleDayTime
import ziox.ramiro.saes.features.saes.features.schedule.data.models.WeekDay
import ziox.ramiro.saes.features.saes.features.schedule.features.edit_class.data.models.EditClassContract
import ziox.ramiro.saes.ui.components.BaseButton
import ziox.ramiro.saes.ui.components.SAESTextField
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import java.util.*

@AndroidEntryPoint
class EditClassActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(!intent.hasExtra(EditClassContract.EditClassInput)) finish()

        val initialClass = intent.getParcelableExtra<ClassSchedule>(EditClassContract.EditClassInput)

        if (initialClass == null){
            finish()
            return
        }

        val originalClass = runBlocking(Dispatchers.Default) {
            LocalAppDatabase.invoke(this@EditClassActivity).scheduleRepository().getClass(initialClass.id)
        }

        setContent {
            SAESParaAlumnosTheme {
                val weekDayOptions = listOf(WeekDay.MONDAY, WeekDay.TUESDAY, WeekDay.WEDNESDAY, WeekDay.THURSDAY, WeekDay.FRIDAY)

                val groupValidator = remember {
                    ValidatorState(initialClass.group){
                        if(it.isBlank()){
                            "El campo está vacío"
                        }else null
                    }
                }

                val buildingValidator = remember {
                    ValidatorState(initialClass.building){
                        if(it.isBlank()){
                            "El campo está vacío"
                        }else null
                    }
                }

                val classroomValidator = remember {
                    ValidatorState(initialClass.classroom){
                        if(it.isBlank()){
                            "El campo está vacío"
                        }else null
                    }
                }

                val teacherNameValidator = remember {
                    ValidatorState(initialClass.teacherName){
                        if(it.isBlank()){
                            "El campo está vacío"
                        }else null
                    }
                }

                val dayOfWeek = remember {
                    mutableStateOf(initialClass.scheduleDayTime.weekDay)
                }

                val startHour = remember {
                    ValidatorState(initialClass.scheduleDayTime.start)
                }

                val endHour = remember {
                    ValidatorState(initialClass.scheduleDayTime.end)
                }

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp, horizontal = 32.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = 32.dp),
                        text = initialClass.className,
                        style = MaterialTheme.typography.h4
                    )
                    SAESTextField(
                        state = groupValidator,
                        label = "Grupo",
                        trailing = originalClass?.group?.let {
                            {
                                if (it != groupValidator.value){
                                    IconButton(
                                        onClick = {
                                            groupValidator.value = it
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.History,
                                            contentDescription = "Undo",
                                            tint = MaterialTheme.colors.primary
                                        )
                                    }
                                }
                            }
                        }
                    )
                    SAESTextField(
                        state = buildingValidator,
                        label = "Edificio",
                        trailing = originalClass?.building?.let {
                            {
                                if (it != buildingValidator.value){
                                    IconButton(
                                        onClick = {
                                            buildingValidator.value = it
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.History,
                                            contentDescription = "Undo",
                                            tint = MaterialTheme.colors.primary
                                        )
                                    }
                                }
                            }
                        }
                    )
                    SAESTextField(
                        state = classroomValidator,
                        label = "Salón",
                        trailing = originalClass?.classroom?.let {
                            {
                                if (it != classroomValidator.value){
                                    IconButton(
                                        onClick = {
                                            classroomValidator.value = it
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.History,
                                            contentDescription = "Undo",
                                            tint = MaterialTheme.colors.primary
                                        )
                                    }
                                }
                            }
                        }
                    )
                    SAESTextField(
                        state = teacherNameValidator,
                        label = "Nombre del profesor/a",
                        trailing = originalClass?.teacherName?.let {
                            {
                                if (it != teacherNameValidator.value){
                                    IconButton(
                                        onClick = {
                                            teacherNameValidator.value = it
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.History,
                                            contentDescription = "Undo",
                                            tint = MaterialTheme.colors.primary
                                        )
                                    }
                                }
                            }
                        }
                    )
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = "Dia de la semana",
                        style = MaterialTheme.typography.subtitle2
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SelectableOptions(
                            options = weekDayOptions,
                            selectionState = dayOfWeek,
                            stringAdapter = {
                                it.dayName
                            }
                        )
                        originalClass?.scheduleDayTime?.weekDay?.let {
                            if (it != dayOfWeek.value){
                                IconButton(
                                    onClick = {
                                        dayOfWeek.value = it
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.History,
                                        contentDescription = "Undo",
                                        tint = MaterialTheme.colors.primary
                                    )
                                }
                            }
                        }
                    }
                    SAESTextField(
                        modifier = Modifier.padding(top = 16.dp),
                        state = startHour,
                        label = "Hora de inicio",
                        readOnly = true,
                        onClick = {
                            showHourPickerDialog(this@EditClassActivity, startHour.value){
                                startHour.value = it
                            }
                        },
                        trailing = originalClass?.scheduleDayTime?.start?.let {
                            {
                                if (it != startHour.value){
                                    IconButton(
                                        onClick = {
                                            startHour.value = it
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.History,
                                            contentDescription = "Undo",
                                            tint = MaterialTheme.colors.primary
                                        )
                                    }
                                }
                            }
                        }
                    )
                    SAESTextField(
                        state = endHour,
                        label = "Hora de finalización",
                        readOnly = true,
                        onClick = {
                            showHourPickerDialog(this@EditClassActivity, endHour.value){
                                endHour.value = it
                            }
                        },
                        trailing = originalClass?.scheduleDayTime?.end?.let {
                            {
                                if (it != endHour.value){
                                    IconButton(
                                        onClick = {
                                            endHour.value = it
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.History,
                                            contentDescription = "Undo",
                                            tint = MaterialTheme.colors.primary
                                        )
                                    }
                                }
                            }
                        }
                    )
                    BaseButton(
                        modifier = Modifier
                            .padding(top = 32.dp)
                            .align(Alignment.CenterHorizontally),
                        isHighEmphasis = true,
                        text = "Guardar"
                    ){
                        if(arrayOf<ValidatorState<*>>(
                                groupValidator,
                                buildingValidator,
                                classroomValidator,
                                teacherNameValidator,
                                startHour,
                                endHour
                        ).validate()){
                            setResult(RESULT_OK, intent.apply {
                                putExtra(EditClassContract.EditClassOutput, CustomClassSchedule(
                                    initialClass.id,
                                    initialClass.classId,
                                    initialClass.className,
                                    groupValidator.value,
                                    buildingValidator.value,
                                    classroomValidator.value,
                                    teacherNameValidator.value,
                                    initialClass.color,
                                    false,
                                    ScheduleDayTime(
                                        weekDay = dayOfWeek.value,
                                        start = startHour.value,
                                        end = endHour.value
                                    )
                                ))
                            })
                            finish()
                        }
                    }
                }
            }
        }
    }
}