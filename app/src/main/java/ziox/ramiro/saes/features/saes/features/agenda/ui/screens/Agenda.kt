package ziox.ramiro.saes.features.saes.features.agenda.ui.screens

import android.text.util.Linkify
import android.widget.TextView
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBackIos
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import ziox.ramiro.saes.R
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.saes.features.agenda.data.models.AgendaCalendar
import ziox.ramiro.saes.features.saes.features.agenda.data.models.AgendaItem
import ziox.ramiro.saes.features.saes.features.agenda.data.repositories.AgendaWebViewRepository
import ziox.ramiro.saes.features.saes.features.agenda.view_models.AgendaListViewModel
import ziox.ramiro.saes.features.saes.features.agenda.view_models.AgendaViewModel
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ShortDate
import ziox.ramiro.saes.features.saes.features.schedule.data.models.getRangeBy
import ziox.ramiro.saes.features.saes.features.schedule.ui.screens.hourWidth
import ziox.ramiro.saes.ui.components.AsyncButton
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.ui.components.ResponsePlaceholder
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
            AgendaView(selectedAgenda)
        }else{
            CalendarList(selectedAgenda = selectedAgenda)
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
    val showAddAgendaDialog = remember {
        mutableStateOf(false)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(bottom = 64.dp),
                onClick = {
                    showAddAgendaDialog.value = true
                }
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add")
            }
        }
    ) {
        if(agendaListViewModel.agendaList.value != null){
            agendaListViewModel.agendaList.value?.let {
                if(it.isNotEmpty()){
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                top = 16.dp,
                                start = 32.dp,
                                end = 32.dp,
                                bottom = 64.dp
                            )
                        ) {
                            it.forEach { calendar ->
                                AgendaListItem(selectedAgenda, calendar)
                            }
                        }
                    }
                }else{
                    ResponsePlaceholder(
                        painter = painterResource(id = R.drawable.logging_off),
                        text = "No has agregado agendas"
                    )
                }
            }
        }else{
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
    if (showAddAgendaDialog.value){
        Dialog(
            onDismissRequest = {
                showAddAgendaDialog.value = false
            }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                val name = remember {
                    mutableStateOf("")
                }
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Nueva agenda",
                        style = MaterialTheme.typography.h5
                    )
                    OutlinedTextField(
                        value = name.component1(),
                        onValueChange = name.component2(),
                        label = {
                            Text(text = "Nombre de la agenda")
                        }
                    )
                    AsyncButton(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .align(Alignment.End),
                        text = "Agregar",
                        isLoading = agendaListViewModel.isAddingAgenda.value
                    ){
                        agendaListViewModel.addAgenda(name.value).invokeOnCompletion {
                            showAddAgendaDialog.value = false
                        }
                    }
                }
            }
        }
    }
    ErrorSnackbar(agendaListViewModel.error)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AgendaListItem(
    selectedAgenda: MutableState<String?>,
    calendar: AgendaCalendar
) = Card(
    modifier = Modifier
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.medium)
        .padding(bottom = 16.dp)
        .clickable {
            selectedAgenda.value = calendar.calendarId
        },
    elevation = 0.dp
) {
    Text(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth(),
        text = calendar.name,
        style = MaterialTheme.typography.h5,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}


@OptIn(ExperimentalTime::class)
@Composable
fun AgendaView(
    selectedAgenda: MutableState<String?>,
    agendaViewModel: AgendaViewModel = viewModel(
        factory = viewModelFactory { AgendaViewModel(AgendaWebViewRepository(LocalContext.current), selectedAgenda.value) }
    )
) {
    Column {
        val today = Date()
        val todayShortDate = ShortDate.fromDate(today)
        val selectedDateIndex = remember {
            mutableStateOf(0)
        }
        val availableDates = List(182){
            ShortDate.fromDate(today.offset(Duration.days(it)))
        }

        Row(
            modifier = Modifier.padding(start = 22.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    selectedAgenda.value = null
                }
            ) {
                Icon(imageVector = Icons.Rounded.ArrowBackIos, contentDescription = "Back")
            }
            Text(
                text = "SALIR",
                style = MaterialTheme.typography.h5
            )
        }

        if(agendaViewModel.eventList.value != null){
            agendaViewModel.eventList.value?.let {
                LazyRow(
                    modifier = Modifier.padding(bottom = 32.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp)
                ) {
                    items(availableDates.size){ i ->
                        DateSelectorItem(
                            date = availableDates[i],
                            today = todayShortDate,
                            isSelected = i == selectedDateIndex.value,
                            events = it.filter { event ->
                                event.date == availableDates[selectedDateIndex.value]
                            }
                        ){
                            selectedDateIndex.value = i
                        }
                    }
                }
                AgendaSchedule(
                    modifier = Modifier.weight(1f),
                    it.filter { event ->
                        event.date == availableDates[selectedDateIndex.value]
                    }
                )
            }
        }else{
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
    ErrorSnackbar(agendaViewModel.error)
}


@Composable
fun AgendaSchedule(
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
    events: List<AgendaItem>,
    onSelect: () -> Unit
) = Column(
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Card(
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
    Row(
        modifier = Modifier.padding(top = 8.dp)
    ) {
        events.take(4).forEach {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(it.classSchedule?.color?.toULong() ?: getCurrentTheme().divider.value))
            )
        }
    }
}