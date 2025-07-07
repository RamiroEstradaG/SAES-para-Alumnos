package ziox.ramiro.saes.features.saes.features.agenda.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.util.Linkify
import android.widget.TextView
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import ziox.ramiro.saes.R
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.features.agenda.data.models.AgendaCalendar
import ziox.ramiro.saes.features.saes.features.agenda.data.models.AgendaEventType
import ziox.ramiro.saes.features.saes.features.agenda.data.models.AgendaItem
import ziox.ramiro.saes.features.saes.features.agenda.data.repositories.AgendaWebViewRepository
import ziox.ramiro.saes.features.saes.features.agenda.view_models.AgendaListViewModel
import ziox.ramiro.saes.features.saes.features.agenda.view_models.AgendaViewModel
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.Hour
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ScheduleDayTime
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ShortDate
import ziox.ramiro.saes.features.saes.features.schedule.data.models.checkIfOccupied
import ziox.ramiro.saes.features.saes.features.schedule.data.models.getRangeBy
import ziox.ramiro.saes.features.saes.features.schedule.data.repositories.ScheduleWebViewRepository
import ziox.ramiro.saes.features.saes.features.schedule.ui.screens.hourWidth
import ziox.ramiro.saes.features.saes.features.schedule.view_models.ScheduleViewModel
import ziox.ramiro.saes.features.saes.ui.components.FlexView
import ziox.ramiro.saes.ui.components.AsyncButton
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.ui.components.OutlineButton
import ziox.ramiro.saes.ui.components.ResponsePlaceholder
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.utils.MES
import ziox.ramiro.saes.utils.MutableStateWithValidation
import ziox.ramiro.saes.utils.offset
import ziox.ramiro.saes.utils.toHour
import ziox.ramiro.saes.utils.validate
import java.util.Date
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

val hourHeight = 128.dp
val eventWidth = 250.dp

@Composable
fun Agenda() {
    val selectedAgenda = remember {
        mutableStateOf<String?>(null)
    }

    Crossfade(targetState = selectedAgenda.value) {
        if (it != null) {
            AgendaView(LocalContext.current, selectedAgenda)
        } else {
            CalendarList(selectedAgenda = selectedAgenda)
        }
    }
}


@Composable
fun CalendarList(
    context: Context = LocalContext.current,
    agendaListViewModel: AgendaListViewModel = viewModel(
        factory = viewModelFactory { AgendaListViewModel(AgendaWebViewRepository(context)) }
    ),
    selectedAgenda: MutableState<String?>
) {
    val showAddAgendaDialog = remember {
        mutableStateOf(false)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddAgendaDialog.value = true
                }
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        if (agendaListViewModel.agendaList.value != null) {
            agendaListViewModel.agendaList.value?.let {
                if (it.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
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
                                AgendaListItem(agendaListViewModel, selectedAgenda, calendar)
                            }
                        }
                    }
                } else {
                    ResponsePlaceholder(
                        painter = painterResource(id = R.drawable.logging_off),
                        text = "No has agregado agendas"
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
    if (showAddAgendaDialog.value) {
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
                val name = MutableStateWithValidation(remember {
                    mutableStateOf("")
                }, remember {
                    mutableStateOf("")
                }) {
                    if (it.isBlank()) {
                        "El campo está vacío"
                    } else null
                }
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Nueva agenda",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    OutlinedTextField(
                        value = name.mutableState.component1(),
                        onValueChange = name.mutableState.component2(),
                        label = {
                            Text(text = "Nombre de la agenda")
                        }
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        color = MaterialTheme.colorScheme.error,
                        text = name.errorState.value ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    AsyncButton(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .align(Alignment.End),
                        text = "Agregar",
                        isLoading = agendaListViewModel.isAddingAgenda.value
                    ) {
                        if (name.validate()) {
                            agendaListViewModel.addAgenda(name.mutableState.value)
                                .invokeOnCompletion {
                                    showAddAgendaDialog.value = false
                                }
                        }
                    }
                }
            }
        }
    }
    ErrorSnackbar(agendaListViewModel.error)
}


@Composable
fun AgendaListItem(
    agendaListViewModel: AgendaListViewModel,
    selectedAgenda: MutableState<String?>,
    calendar: AgendaCalendar
) = Card(
    modifier = Modifier
        .fillMaxWidth()
        .height(80.dp)
        .clip(MaterialTheme.shapes.medium)
        .padding(bottom = 16.dp)
        .clickable {
            selectedAgenda.value = calendar.calendarId
        },
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
) {
    Row(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .weight(1f),
            text = calendar.name,
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (agendaListViewModel.isRemovingAgenda.value.contains(calendar.calendarId)) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp)
            )
        } else {
            IconButton(
                onClick = {
                    agendaListViewModel.removeAgenda(calendar.calendarId)
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete agenda",
                    tint = getCurrentTheme().danger
                )
            }
        }
    }
}


@OptIn(ExperimentalTime::class)
@Composable
fun AgendaView(
    context: Context = LocalContext.current,
    selectedAgenda: MutableState<String?>,
    scheduleViewModel: ScheduleViewModel = viewModel(
        factory = viewModelFactory {
            ScheduleViewModel(
                ScheduleWebViewRepository(context),
                LocalAppDatabase.invoke(context).customScheduleGeneratorRepository()
            )
        }
    ),
    agendaViewModel: AgendaViewModel = viewModel(
        factory = viewModelFactory {
            AgendaViewModel(
                AgendaWebViewRepository(context),
                selectedAgenda.value
            )
        },
        key = selectedAgenda.value
    )
) {
    val context = LocalContext.current
    val today = Date()
    val todayShortDate = ShortDate.fromDate(today)
    val showAddEventDialog = remember {
        mutableStateOf(false)
    }
    val selectedDateIndex = remember {
        mutableStateOf(0)
    }
    val availableDates = List(182) {
        ShortDate.fromDate(today.offset(it.days))
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(bottom = 64.dp),
                onClick = {
                    showAddEventDialog.value = true
                }
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add event")
            }
        }
    ) { paddingValues ->
        Column {
            Row(
                modifier = Modifier.padding(paddingValues),
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
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            if (agendaViewModel.eventList.value != null) {
                agendaViewModel.eventList.value?.let {
                    LazyRow(
                        modifier = Modifier.padding(bottom = 8.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp)
                    ) {
                        items(availableDates.size) { i ->
                            DateSelectorItem(
                                date = availableDates[i],
                                today = todayShortDate,
                                isSelected = i == selectedDateIndex.value,
                                events = it.filter { event ->
                                    event.date == availableDates[i]
                                }
                            ) {
                                selectedDateIndex.value = i
                            }
                        }
                    }
                    AgendaSchedule(
                        modifier = Modifier.weight(1f),
                        it.filter { event ->
                            event.date == availableDates[selectedDateIndex.value]
                        },
                        agendaViewModel
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
    if (showAddEventDialog.value) {
        Dialog(
            onDismissRequest = {
                showAddEventDialog.value = false
            }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    val name = MutableStateWithValidation(remember {
                        mutableStateOf("")
                    }, remember {
                        mutableStateOf(null)
                    }) {
                        if (it.isBlank()) {
                            "El campo está vacío"
                        } else null
                    }

                    val date = MutableStateWithValidation(remember {
                        mutableStateOf(ShortDate.fromDate(Date()))
                    }, remember {
                        mutableStateOf(null)
                    }) {
                        if (it.toDate().before(ShortDate.fromDate(Date()).toDate())) {
                            "La fecha no es válida"
                        } else null
                    }

                    val hourRange = MutableStateWithValidation(remember {
                        mutableStateOf<Pair<Hour, Hour>?>(null)
                    }, remember {
                        mutableStateOf(null)
                    }) {
                        if (it == null) {
                            "El rango de horas no es válido"
                        } else null
                    }
                    val description = remember {
                        mutableStateOf("")
                    }
                    val selectedClassSchedule = remember {
                        mutableStateOf<ClassSchedule?>(null)
                    }

                    Text(
                        text = "Agregar evento",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Column(
                        modifier = Modifier
                            .heightIn(0.dp, 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.padding(top = 8.dp),
                            value = name.mutableState.component1(),
                            onValueChange = name.mutableState.component2(),
                            label = {
                                Text(text = "Título")
                            },
                            isError = !name.errorState.value.isNullOrBlank()
                        )
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            color = MaterialTheme.colorScheme.error,
                            text = name.errorState.value ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedTextField(
                            modifier = Modifier.padding(top = 8.dp),
                            value = description.component1(),
                            onValueChange = description.component2(),
                            label = {
                                Text(text = "Descripción")
                            },
                        )
                        Box(
                            modifier = Modifier.padding(top = 16.dp),
                        ) {
                            OutlinedTextField(
                                value = date.mutableState.component1().toString(),
                                onValueChange = {},
                                label = {
                                    Text(text = "Fecha")
                                },
                                readOnly = true,
                                isError = !date.errorState.value.isNullOrBlank()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .clickable {
                                        showDatePickerDialog(context) {
                                            date.mutableState.value = it
                                        }
                                    }
                            )
                        }
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            color = MaterialTheme.colorScheme.error,
                            text = date.errorState.value ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Box(
                            modifier = Modifier.padding(top = 8.dp),
                        ) {
                            OutlinedTextField(
                                value = hourRange.mutableState.component1()?.toString() ?: "",
                                onValueChange = {},
                                label = {
                                    Text(text = "Rango de horas")
                                },
                                readOnly = true,
                                isError = !hourRange.errorState.value.isNullOrBlank()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .clickable {
                                        showHourRangePickerDialog(context) {
                                            hourRange.mutableState.value = it
                                        }
                                    }
                            )
                        }
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            color = MaterialTheme.colorScheme.error,
                            text = hourRange.errorState.value ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (scheduleViewModel.scheduleList.isNotEmpty()) {
                            SelectAddAgendaEventList(
                                title = "Vincular a una clase",
                                options = scheduleViewModel.scheduleList
                            ) { newItem ->
                                selectedClassSchedule.value = newItem
                            }
                        }
                    }
                    AsyncButton(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 16.dp),
                        text = "Agregar",
                        isLoading = agendaViewModel.isAddAgendaLoading.value
                    ) {
                        if (listOf(name, date, hourRange).validate()) {
                            agendaViewModel.addAgendaEvent(
                                AgendaItem(
                                    eventName = name.mutableState.value,
                                    eventType = AgendaEventType.PERSONAL,
                                    date = date.mutableState.value,
                                    scheduleDayTime = ScheduleDayTime(
                                        hourRange.mutableState.value!!.first,
                                        hourRange.mutableState.value!!.second,
                                    ),
                                    calendarId = selectedAgenda.value!!,
                                    description = description.value,
                                    classSchedule = selectedClassSchedule.value,
                                )
                            ).invokeOnCompletion {
                                showAddEventDialog.value = false
                            }
                        }
                    }
                }
            }
        }
    }
    ErrorSnackbar(agendaViewModel.error)
}

fun showHourPickerDialog(context: Context, default: Hour? = null, onChange: (Hour) -> Unit) {
    TimePickerDialog(context, { _, hour2, minute2 ->
        onChange(Hour(hour2, minute2))
    }, default?.hours ?: 12, default?.minutes ?: 0, false).show()
}

fun showHourRangePickerDialog(context: Context, onChange: (Pair<Hour, Hour>) -> Unit) {
    showHourPickerDialog(context) { h1 ->
        showHourPickerDialog(context) { h2 ->
            onChange(Pair(h1, h2))
        }
    }
}

fun showDatePickerDialog(context: Context, onChange: (ShortDate) -> Unit) {
    val today = ShortDate.fromDate(Date())
    DatePickerDialog(context, { _, year, month, day ->
        onChange(ShortDate(day, month, year))
    }, today.year, today.month, today.day).show()
}

@Composable
fun <T> SelectableOptions(
    options: List<T>?,
    stringAdapter: (T) -> String = { it.toString() },
    initialSelection: Int? = null,
    onSelectionChange: (T?) -> Unit
) {
    val infoColor = MaterialTheme.colorScheme.secondary
    val selectedIndex = remember {
        mutableStateOf(initialSelection)
    }

    FlexView(
        content = options?.mapIndexed { i, value ->
            {
                OutlineButton(
                    modifier = Modifier.padding(end = 8.dp, top = 8.dp),
                    text = stringAdapter(value),
                    borderColor = infoColor,
                    textColor = if (i != selectedIndex.value) infoColor else MaterialTheme.colorScheme.onPrimary,
                    backgroundColor = if (i == selectedIndex.value) infoColor else null
                ) {
                    val newIndex = if (selectedIndex.value != i) {
                        i
                    } else null

                    selectedIndex.value = newIndex

                    onSelectionChange(if (newIndex == null) null else options.getOrNull(newIndex))
                }
            }
        } ?: listOf()
    )
}

@Composable
fun SelectAddAgendaEventList(
    title: String,
    options: List<ClassSchedule>?,
    initialSelection: Int? = null,
    onSelectionChange: (ClassSchedule?) -> Unit
) = Column(
    modifier = Modifier.padding(bottom = 16.dp)
) {


    Text(
        text = if (options?.isNotEmpty() == true) title else "",
        style = MaterialTheme.typography.titleMedium
    )
    SelectableOptions(
        options = options,
        initialSelection = initialSelection,
        onSelectionChange = onSelectionChange,
        stringAdapter = {
            it.className
        }
    )
}


@Composable
fun AgendaSchedule(
    modifier: Modifier = Modifier,
    events: List<AgendaItem>,
    agendaViewModel: AgendaViewModel
) {

    val xScrollState = rememberScrollState()
    val yScrollState = rememberScrollState()

    Box {
        Row(
            modifier = Modifier
                .verticalScroll(yScrollState)
                .focusable(false)
        ) {
            val hourRange = events.getRangeBy { it.scheduleDayTime }

            HourColumn(hourRange)
            Box(
                modifier = Modifier
                    .horizontalScroll(xScrollState)
                    .focusable(false)
            ) {
                EventsContainer(hourRange, rearrangeList(events), agendaViewModel)
            }
        }
        Box(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        xScrollState.dispatchRawDelta(-dragAmount.x)
                        yScrollState.dispatchRawDelta(-dragAmount.y)
                    }
                }
        )
    }
}


private fun rearrangeList(events: List<AgendaItem>): List<List<AgendaItem>> {
    val columns = ArrayList<ArrayList<AgendaItem>>()
    columns.add(arrayListOf())
    var currentColumnsLength = columns.size

    val sorted = events.sortedByDescending {
        it.scheduleDayTime.duration
    }

    sorted.forEach { agendaItem ->
        var i = 0
        while (i < currentColumnsLength) {
            val column = columns[i++]

            if (checkIfOccupied(
                    column.map { it.scheduleDayTime },
                    agendaItem.scheduleDayTime
                ) != null
            ) {
                if (i >= currentColumnsLength) {
                    columns.add(arrayListOf(agendaItem))
                    currentColumnsLength = columns.size
                    break
                }
            } else {
                column.add(agendaItem)
                break
            }
        }
    }

    return columns
}

@Composable
fun EventsContainer(
    hourRange: IntRange,
    events: List<List<AgendaItem>>,
    agendaViewModel: AgendaViewModel
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
                        top = hourHeight.times((item.scheduleDayTime.start.toDouble() - hourRange.first).toFloat()),
                        start = eventWidth.times(i)
                    )
                    .size(
                        eventWidth,
                        hourHeight.times(item.scheduleDayTime.duration.toFloat())
                    )

            ) {
                EventCard(item, agendaViewModel)
            }
        }
    }
}

@Composable
fun EventCard(
    agendaItem: AgendaItem,
    agendaViewModel: AgendaViewModel
) = Card(
    modifier = Modifier
        .padding(horizontal = 8.dp)
        .fillMaxSize(),
    shape = MaterialTheme.shapes.small,
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    colors = CardDefaults.cardColors(
        containerColor = if (agendaItem.classSchedule != null) {
            Color(agendaItem.classSchedule.color.toULong()).copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surface
        }
    ),
) {
    val secondaryText = getCurrentTheme().secondaryText

    Column(
        Modifier
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = agendaItem.eventName,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            /*if(agendaViewModel.isRemovingEvent.value == agendaItem.eventId){
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            }else{
                IconButton(
                    modifier = Modifier.size(22.dp),
                    onClick = {
                        agendaViewModel.removeEvent(agendaItem.eventId)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete agenda",
                        tint = getCurrentTheme().danger
                    )
                }
            }*/
        }
        AndroidView(
            modifier = Modifier.verticalScroll(rememberScrollState()),
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
        HorizontalDivider()
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
            style = MaterialTheme.typography.titleLarge
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
            .size(64.dp, 80.dp)
            .padding(end = 8.dp)
            .clickable { onSelect() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else if (date == today) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
        ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = MES[date.month].uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else if (date == today) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = date.day.toString(),
                style = MaterialTheme.typography.headlineLarge,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else if (date == today) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
    Row(
        modifier = Modifier.padding(top = 4.dp, end = 8.dp)
    ) {
        events.take(4).forEach {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        Color(
                            it.classSchedule?.color?.toULong() ?: getCurrentTheme().divider.value
                        )
                    )
            )
        }
    }
}