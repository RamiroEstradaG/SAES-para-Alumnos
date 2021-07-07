package ziox.ramiro.saes.features.saes.features.schedule.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.filter
import ziox.ramiro.saes.R
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.repositories.ScheduleWebViewRepository
import ziox.ramiro.saes.features.saes.features.schedule.view_models.ScheduleState
import ziox.ramiro.saes.features.saes.features.schedule.view_models.ScheduleViewModel
import ziox.ramiro.saes.ui.components.ResponsePlaceholder
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.utils.getInitials
import ziox.ramiro.saes.utils.toHour

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

@Composable
fun ScheduleHeader(
    selectedDayOfWeek: MutableState<ClassSchedule.WeekDay?> = mutableStateOf(null)
) = Crossfade(
    targetState = selectedDayOfWeek.value
) {
    if(it != null){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, top = 4.dp),
        ) {
            Text(
                modifier = Modifier.width(hourWidth),
                text = "Hora",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.subtitle2
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = selectedDayOfWeek.value?.dayName ?: "",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.subtitle2,
                color = if(today == it) getCurrentTheme().info
                else getCurrentTheme().primaryText
            )
        }

    }else{
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, top = 4.dp),
        ) {
            Text(
                modifier = Modifier.width(hourWidth),
                text = "Hora",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.subtitle2
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "L",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.subtitle2,
                color = if(today == ClassSchedule.WeekDay.MONDAY) getCurrentTheme().info
                else getCurrentTheme().primaryText
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "M",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.subtitle2,
                color = if(today == ClassSchedule.WeekDay.TUESDAY) getCurrentTheme().info
                else getCurrentTheme().primaryText
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "M",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.subtitle2,
                color = if(today == ClassSchedule.WeekDay.WEDNESDAY) getCurrentTheme().info
                else getCurrentTheme().primaryText
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "J",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.subtitle2,
                color = if(today == ClassSchedule.WeekDay.THURSDAY) getCurrentTheme().info
                else getCurrentTheme().primaryText
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "V",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.subtitle2,
                color = if(today == ClassSchedule.WeekDay.FRIDAY) getCurrentTheme().info
                else getCurrentTheme().primaryText
            )
        }
    }
}



@Composable
fun HourColumn(
    hourRange: IntRange,
    classSchedules: List<ClassSchedule>
) = Column(
    modifier = Modifier.size(hourWidth, classSchedules.getHourHeight() * (hourRange.last - hourRange.first))
) {
    hourRange.forEach {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .height(classSchedules.getHourHeight()),
            text = it.toDouble().toHour(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.subtitle1
        )
    }
}

@Composable
fun ScheduleWeekContainer(
    classSchedules: List<ClassSchedule>,
    selectedDayOfWeek: MutableState<ClassSchedule.WeekDay?> = mutableStateOf(null)
) {
    val hourRange = classSchedules.getHourRange()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        HourColumn(
            hourRange,
            classSchedules
        )
        ScheduleDayContainer(
            selectedDayOfWeek = selectedDayOfWeek,
            modifier = Modifier.animateContentSize().run {
                if(selectedDayOfWeek.value == null || selectedDayOfWeek.value == ClassSchedule.WeekDay.MONDAY){
                    weight(1f)
                }else{
                    width(0.dp)
                }
            },
            weekDay = ClassSchedule.WeekDay.MONDAY,
            classSchedules = classSchedules,
            hourRange = hourRange
        ){
            selectedDayOfWeek.value = if(selectedDayOfWeek.value != it){
                it
            }else null
        }
        ScheduleDayContainer(
            selectedDayOfWeek = selectedDayOfWeek,
            weekDay = ClassSchedule.WeekDay.TUESDAY,
            modifier = Modifier.animateContentSize().run {
                if(selectedDayOfWeek.value == null || selectedDayOfWeek.value == ClassSchedule.WeekDay.TUESDAY){
                    weight(1f)
                }else{
                    width(0.dp)
                }
            },
            classSchedules = classSchedules,
            hourRange = hourRange
        ){
            selectedDayOfWeek.value = if(selectedDayOfWeek.value != it){
                it
            }else null
        }
        ScheduleDayContainer(
            selectedDayOfWeek = selectedDayOfWeek,
            modifier = Modifier.animateContentSize().run {
                if(selectedDayOfWeek.value == null || selectedDayOfWeek.value == ClassSchedule.WeekDay.WEDNESDAY){
                    weight(1f)
                }else{
                    width(0.dp)
                }
            },
            weekDay = ClassSchedule.WeekDay.WEDNESDAY,
            classSchedules = classSchedules,
            hourRange = hourRange
        ){
            selectedDayOfWeek.value = if(selectedDayOfWeek.value != it){
                it
            }else null
        }
        ScheduleDayContainer(
            selectedDayOfWeek = selectedDayOfWeek,
            modifier = Modifier.animateContentSize().run {
                if(selectedDayOfWeek.value == null || selectedDayOfWeek.value == ClassSchedule.WeekDay.THURSDAY){
                    weight(1f)
                }else{
                    width(0.dp)
                }
            },
            weekDay = ClassSchedule.WeekDay.THURSDAY,
            classSchedules = classSchedules,
            hourRange = hourRange
        ){
            selectedDayOfWeek.value = if(selectedDayOfWeek.value != it){
                it
            }else null
        }
        ScheduleDayContainer(
            selectedDayOfWeek = selectedDayOfWeek,
            modifier = Modifier.animateContentSize().run {
                if(selectedDayOfWeek.value == null || selectedDayOfWeek.value == ClassSchedule.WeekDay.FRIDAY){
                    weight(1f)
                }else{
                    width(0.dp)
                }
            },
            weekDay = ClassSchedule.WeekDay.FRIDAY,
            classSchedules = classSchedules,
            hourRange = hourRange
        ){
            selectedDayOfWeek.value = if(selectedDayOfWeek.value != it){
                it
            }else null
        }
    }
}

@Composable
fun ScheduleDayContainer(
    modifier: Modifier = Modifier,
    classSchedules: List<ClassSchedule>,
    weekDay: ClassSchedule.WeekDay,
    selectedDayOfWeek: MutableState<ClassSchedule.WeekDay?> = mutableStateOf(null),
    hourRange: IntRange,
    onClick: (ClassSchedule.WeekDay) -> Unit
) = Box(
    modifier = modifier
        .height(
            classSchedules
                .getHourHeight()
                .times(hourRange.last - hourRange.first)
        )
        .clickable(
            interactionSource = MutableInteractionSource(),
            onClick = {
                onClick(weekDay)
            },
            indication = rememberRipple()
        )
) {
    classSchedules.filter {
        it.hour.weekDay == weekDay
    }.forEach {
        ScheduleClassView(
            isExpanded = selectedDayOfWeek.value == weekDay,
            classSchedule = it,
            startHour = hourRange.first,
            hourHeight = classSchedules.getHourHeight()
        )
    }
}

@Composable
fun ScheduleClassView(
    isExpanded: Boolean = false,
    startHour: Int,
    classSchedule: ClassSchedule,
    hourHeight: Dp = 170.dp
) = Card(
    modifier = Modifier
        .padding(top = hourHeight.times((classSchedule.hour.start - startHour).toFloat()))
        .height(hourHeight.times(classSchedule.hour.duration.toFloat()))
        .fillMaxWidth(),
    backgroundColor = classSchedule.color,
    shape = MaterialTheme.shapes.small,
    elevation = 0.dp
) {
    Crossfade(targetState = isExpanded) {
        if(it){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = classSchedule.className,
                    color = MaterialTheme.colors.onPrimary,
                    style = MaterialTheme.typography.h5,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = "Profesor/a",
                    color = MaterialTheme.colors.onPrimary,
                    style = MaterialTheme.typography.caption,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = classSchedule.teacherName,
                    color = MaterialTheme.colors.onPrimary,
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.padding(top = 20.dp),
                ) {
                    Column {
                        Text(
                            text = "Edificio",
                            color = MaterialTheme.colors.onPrimary,
                            style = MaterialTheme.typography.caption,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = classSchedule.building,
                            color = MaterialTheme.colors.onPrimary,
                            style = MaterialTheme.typography.subtitle1,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Column(
                        Modifier.padding(start = 32.dp)
                    ) {
                        Text(
                            text = "Salón",
                            color = MaterialTheme.colors.onPrimary,
                            style = MaterialTheme.typography.caption,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = classSchedule.classroom,
                            color = MaterialTheme.colors.onPrimary,
                            style = MaterialTheme.typography.subtitle1,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

            }
        }else{
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = classSchedule.className.getInitials(),
                    color = MaterialTheme.colors.onPrimary,
                    style = MaterialTheme.typography.h5,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    modifier = Modifier.padding(top = 12.dp),
                    text = classSchedule.building,
                    color = MaterialTheme.colors.onPrimary,
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = classSchedule.classroom,
                    color = MaterialTheme.colors.onPrimary,
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
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