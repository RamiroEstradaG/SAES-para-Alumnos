package ziox.ramiro.saes.features.saes.features.schedule.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.map
import ziox.ramiro.saes.R
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.WeekDay
import ziox.ramiro.saes.features.saes.features.schedule.ui.components.ScheduleHeader
import ziox.ramiro.saes.features.saes.features.schedule.ui.components.ScheduleWeekContainer
import ziox.ramiro.saes.features.saes.features.schedule.view_models.ScheduleViewModel
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.ui.components.ResponsePlaceholder
import ziox.ramiro.saes.utils.updateWidgets

val hourWidth = 70.dp
val today = WeekDay.today()

@Composable
fun Schedule(
    scheduleViewModel: ScheduleViewModel = viewModel()
) {
    if (!scheduleViewModel.isLoading.value) {
        LocalContext.current.updateWidgets()
        if (scheduleViewModel.scheduleList.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp),
            ) {
                val selectedDayOfWeek: MutableState<WeekDay?> = remember {
                    mutableStateOf(null)
                }

                ScheduleHeader(
                    selectedDayOfWeek = selectedDayOfWeek
                )
                ScheduleWeekContainer(
                    classSchedules = scheduleViewModel.scheduleList,
                    selectedDayOfWeek = selectedDayOfWeek,
                    isClassActionsEnabled = true
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
            ) {
                ResponsePlaceholder(
                    painter = painterResource(id = R.drawable.logging_off),
                    text = "No tienes ninguna materia registrada"
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    ErrorSnackbar(scheduleViewModel.error)
    ErrorSnackbar(scheduleViewModel.scrapError.map { it?.let { "Error al cargar el horario" } }) {
        scheduleViewModel.uploadSourceCode()
    }
}


fun List<ClassSchedule>.getHourHeight(): Dp = 170.dp.div((minByOrNull {
    it.scheduleDayTime.duration
}?.scheduleDayTime?.duration?.toFloat() ?: 1f))