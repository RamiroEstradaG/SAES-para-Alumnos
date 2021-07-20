package ziox.ramiro.saes.features.saes.features.schedule.ui.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
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
import ziox.ramiro.saes.R
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.WeekDay
import ziox.ramiro.saes.features.saes.features.schedule.data.repositories.ScheduleWebViewRepository
import ziox.ramiro.saes.features.saes.features.schedule.ui.components.ScheduleHeader
import ziox.ramiro.saes.features.saes.features.schedule.ui.components.ScheduleWeekContainer
import ziox.ramiro.saes.features.saes.features.schedule.view_models.ScheduleViewModel
import ziox.ramiro.saes.ui.components.ResponsePlaceholder

val hourWidth = 70.dp
val today = WeekDay.today()

@ExperimentalAnimationApi
@Composable
fun Schedule(
    scheduleViewModel: ScheduleViewModel = viewModel(
        factory = viewModelFactory { ScheduleViewModel(ScheduleWebViewRepository(LocalContext.current)) }
    )
) {
    if(scheduleViewModel.scheduleList.value != null){
        scheduleViewModel.scheduleList.value?.let {
            if(it.isNotEmpty()){
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val selectedDayOfWeek: MutableState<WeekDay?> = remember {
                        mutableStateOf(null)
                    }

                    ScheduleHeader(
                        selectedDayOfWeek = selectedDayOfWeek
                    )
                    ScheduleWeekContainer(
                        classSchedules = it,
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
        }
    }else{
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
}


fun List<ClassSchedule>.getHourHeight(): Dp = 170.dp.div((minByOrNull {
    it.hourRange.duration
}?.hourRange?.duration?.toFloat() ?: 1f))