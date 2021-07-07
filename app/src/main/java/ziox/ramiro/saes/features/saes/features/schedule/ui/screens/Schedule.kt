package ziox.ramiro.saes.features.saes.features.schedule.ui.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.filter
import ziox.ramiro.saes.R
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.repositories.ScheduleWebViewRepository
import ziox.ramiro.saes.features.saes.features.schedule.ui.components.ScheduleHeader
import ziox.ramiro.saes.features.saes.features.schedule.ui.components.ScheduleWeekContainer
import ziox.ramiro.saes.features.saes.features.schedule.view_models.ScheduleState
import ziox.ramiro.saes.features.saes.features.schedule.view_models.ScheduleViewModel
import ziox.ramiro.saes.ui.components.ResponsePlaceholder

val hourWidth = 70.dp
val today = ClassSchedule.WeekDay.todayByCalendar()

@ExperimentalAnimationApi
@Composable
fun Schedule(
    scheduleViewModel: ScheduleViewModel = viewModel(
        factory = viewModelFactory { ScheduleViewModel(ScheduleWebViewRepository(LocalContext.current)) }
    )
) = when(val state = scheduleViewModel.states.filter {
    it is ScheduleState.ScheduleLoading || it is ScheduleState.ScheduleComplete
}.collectAsState(initial = null).value){
    is ScheduleState.ScheduleComplete -> if(state.schedules.isNotEmpty()){
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            val selectedDayOfWeek: MutableState<ClassSchedule.WeekDay?> = remember {
                mutableStateOf(null)
            }

            ScheduleHeader(
                selectedDayOfWeek = selectedDayOfWeek
            )
            ScheduleWeekContainer(
                classSchedules = state.schedules,
                selectedDayOfWeek = selectedDayOfWeek
            )
        }
    }else{
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)) {
            ResponsePlaceholder(
                painter = painterResource(id = R.drawable.logging_off),
                text = "No tienes ninguna materia registrada"
            )
        }
    }
    is ScheduleState.ScheduleLoading -> Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
    }
    else -> Box {
        scheduleViewModel.fetchMySchedule()
    }
}


fun List<ClassSchedule>.getHourRange(): IntRange {
    var start : Double? = null
    var end : Double? = null

    for (classSchedule in this) {
        if(start?.compareTo(classSchedule.hour.start) ?: 1 > 0){
            start = classSchedule.hour.start
        }

        if(end?.compareTo(classSchedule.hour.end) ?: -1 < 0){
            end = classSchedule.hour.end
        }
    }

    val first = start?.toInt() ?: 0
    val last = end?.toInt() ?: 0

    val diff = last - first

    return IntRange(first, last + if(diff < 6) 6 - diff else 1)
}


fun List<ClassSchedule>.getHourHeight(): Dp = 170.dp.div((minByOrNull {
    it.hour.duration
}?.hour?.duration?.toFloat() ?: 1f))