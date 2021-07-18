package ziox.ramiro.saes.features.saes.features.agenda.ui.screens

import android.text.util.Linkify
import android.widget.TextView
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.saes.features.agenda.data.models.AgendaItem
import ziox.ramiro.saes.features.saes.features.agenda.data.repositories.AgendaWebViewRepository
import ziox.ramiro.saes.features.saes.features.agenda.view_models.AgendaListState
import ziox.ramiro.saes.features.saes.features.agenda.view_models.AgendaListViewModel
import ziox.ramiro.saes.features.saes.features.agenda.view_models.AgendaState
import ziox.ramiro.saes.features.saes.features.agenda.view_models.AgendaViewModel
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ShortDate
import ziox.ramiro.saes.features.saes.features.schedule.data.models.getRangeBy
import ziox.ramiro.saes.features.saes.features.schedule.ui.screens.hourWidth
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.utils.MES
import ziox.ramiro.saes.utils.offset
import ziox.ramiro.saes.utils.toHour
import java.util.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

val hourHeight = 128.dp
val eventWidth = 250.dp





@Composable
fun Agenda(){
    val selectedAgenda = remember {
        mutableStateOf<String?>(null)
    }

    Crossfade(targetState = selectedAgenda.value) {
        if(it != null){
            AgendaView(it)
        }else{
            CalendarList(
                selectedAgenda = selectedAgenda
            )
        }
    }
}


@Composable
fun CalendarList(
    agendaListViewModel: AgendaListViewModel = viewModel(
        factory = viewModelFactory { AgendaListViewModel(AgendaWebViewRepository(LocalContext.current)) }
    ),
    selectedAgenda: MutableState<String?>
){
    when(val state = agendaListViewModel.statesAsState().value){
        is AgendaListState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        is AgendaListState.Complete -> LazyRow {
            items(state.events.size){ i ->
                Card(
                    modifier = Modifier.clickable {
                        selectedAgenda.value = state.events[i].calendarId
                    }
                ) {
                    Text(text = state.events[i].name)
                }
            }
        }
        null -> agendaListViewModel.fetchAgendas()
    }
}


@OptIn(ExperimentalTime::class)
@Composable
fun AgendaView(
    calendarId: String,
    agendaViewModel: AgendaViewModel = viewModel(
        factory = viewModelFactory { AgendaViewModel(AgendaWebViewRepository(LocalContext.current)) }
    )
) = Column {
    val today = Date()
    val todayShortDate = ShortDate.fromDate(today)
    val selectedDateIndex = remember {
        mutableStateOf(0)
    }
    val availableDates = List(182){
        ShortDate.fromDate(today.offset(Duration.days(it)))
    }

    LazyRow(
        modifier = Modifier.padding(bottom = 32.dp),
        contentPadding = PaddingValues(horizontal = 32.dp)
    ) {
        items(availableDates.size){ i ->
            DateSelectorItem(
                date = availableDates[i],
                today = todayShortDate,
                isSelected = i == selectedDateIndex.value
            ){
                selectedDateIndex.value = i
            }
        }
    }

    when(val state = agendaViewModel.statesAsState().value){
        is AgendaState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        is AgendaState.Complete -> Schedule(
            modifier = Modifier.weight(1f),
            state.events.filter {
                it.date == availableDates[selectedDateIndex.value]
            }
        )
        null -> agendaViewModel.fetchAgendaEvents(calendarId)
    }
}


@Composable
fun Schedule(
    modifier: Modifier = Modifier,
    events: List<AgendaItem>
) {

    val xScrollState = rememberScrollState()
    val yScrollState = rememberScrollState()

    Box {
        Row(
            modifier = Modifier
                .verticalScroll(yScrollState)
                .focusable(false)
        ){
            val hourRange = events.getRangeBy { it.hourRange }

            HourColumn(hourRange)
            Box(
                modifier = Modifier
                    .horizontalScroll(xScrollState)
                    .focusable(false)
            ){
                EventsContainer(hourRange, rearrangeList(events))
            }
        }
        Box(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consumeAllChanges()
                        xScrollState.dispatchRawDelta(-dragAmount.x)
                        yScrollState.dispatchRawDelta(-dragAmount.y)
                    }
                }
        )
    }
}


private fun rearrangeList(events: List<AgendaItem>) : List<List<AgendaItem>>{
    val columns = ArrayList<ArrayList<AgendaItem>>()
    columns.add(arrayListOf())
    var currentColumnsLength = columns.size

    val sorted = events.sortedByDescending {
        it.hourRange.duration
    }

    sorted.forEach { agendaItem ->
        var i = 0
        while (i < currentColumnsLength){
            val column = columns[i++]

            if(checkIfOccupied(column, agendaItem)){
                if(i >= currentColumnsLength){
                    columns.add(arrayListOf(agendaItem))
                    currentColumnsLength = columns.size
                    break
                }
            }else{
                column.add(agendaItem)
                break
            }
        }
    }

    return columns
}


private fun checkIfOccupied(list: List<AgendaItem>, item: AgendaItem): Boolean{
    for (agendaItem in list) {
        if(item.hourRange.start.toDouble() in agendaItem.hourRange.start.toDouble()..(agendaItem.hourRange.end.toDouble() - 0.001)
            || item.hourRange.end.toDouble() in (agendaItem.hourRange.start.toDouble() + 0.001)..agendaItem.hourRange.end.toDouble()){
            return true
        }
    }

    return false
}


@Composable
fun EventsContainer(
    hourRange: IntRange,
    events: List<List<AgendaItem>>
) = Box {
    HourDividers(
        modifier = Modifier.fillMaxWidth(),
        hourRange
    )
    events.forEachIndexed { i, it ->
        it.forEach { item ->
            Box(
                modifier = Modifier
                    .padding(
                        top = hourHeight.times((item.hourRange.start.toDouble() - hourRange.first).toFloat()),
                        start = eventWidth.times(i)
                    )
                    .size(
                        eventWidth,
                        hourHeight.times(item.hourRange.duration.toFloat())
                    )

            ) {
                EventCard(item)
            }
        }
    }
}

@Composable
fun EventCard(
    agendaItem: AgendaItem
) = Card(
    modifier = Modifier
        .padding(horizontal = 8.dp)
        .fillMaxSize(),
    shape = MaterialTheme.shapes.small,
    elevation = 0.dp
) {
    val secondaryText = getCurrentTheme().secondaryText

    Column(
        Modifier
            .padding(16.dp)
            .background(
                if (agendaItem.classSchedule != null) {
                    Color(agendaItem.classSchedule.color).copy(alpha = 0.1f)
                } else {
                    Color.Transparent
                }
            )
    ) {
        Text(
            text = agendaItem.eventName,
            style = MaterialTheme.typography.h5,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        AndroidView(
            factory = {
                TextView(it).apply {
                    text = agendaItem.description
                    Linkify.addLinks(this, Linkify.ALL)
                    linksClickable = true
                    setTextColor(secondaryText.toArgb())
                }
            }
        )
    }
}


@Composable
fun HourDividers(
    modifier: Modifier = Modifier,
    hourRange: IntRange
) = Column(
    modifier = modifier
        .height(hourHeight * (hourRange.last - hourRange.first))
) {
    hourRange.forEach { _ ->
        Divider()
        Box(
            modifier = Modifier
                .height(127.dp)
        )
    }
}

@Composable
fun HourColumn(
    hourRange: IntRange
) = Column(
    modifier = Modifier.size(hourWidth, hourHeight * (hourRange.last - hourRange.first))
) {
    hourRange.forEach {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .height(hourHeight),
            text = it.toDouble().toHour(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.subtitle1
        )
    }
}

@Composable
fun DateSelectorItem(
    date: ShortDate,
    today: ShortDate = ShortDate.fromDate(Date()),
    isSelected: Boolean,
    onSelect: () -> Unit
) = Card(
    modifier = Modifier
        .clip(MaterialTheme.shapes.medium)
        .padding(end = 8.dp)
        .size(64.dp, 80.dp)
        .clickable { onSelect() },
    elevation = 0.dp,
    backgroundColor = if (isSelected) MaterialTheme.colors.primary else if(date == today) MaterialTheme.colors.secondary else MaterialTheme.colors.surface
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = MES[date.month].uppercase(),
            style = MaterialTheme.typography.h5,
            color = if (isSelected) MaterialTheme.colors.onPrimary else if(date == today) MaterialTheme.colors.onSecondary else MaterialTheme.colors.onSurface
        )
        Text(
            text = date.day.toString(),
            style = MaterialTheme.typography.h4,
            color = if (isSelected) MaterialTheme.colors.onPrimary else if(date == today) MaterialTheme.colors.onSecondary else MaterialTheme.colors.onSurface
        )
    }
}