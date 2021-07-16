package ziox.ramiro.saes.features.saes.features.agenda.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusOrder
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import ziox.ramiro.saes.features.saes.features.agenda.data.models.AgendaItem
import ziox.ramiro.saes.features.saes.features.schedule.data.models.HourRange
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ShortDate
import ziox.ramiro.saes.features.saes.features.schedule.ui.screens.hourWidth
import ziox.ramiro.saes.utils.MES
import ziox.ramiro.saes.utils.offset
import ziox.ramiro.saes.utils.toHour
import ziox.ramiro.saes.utils.toLongString
import java.util.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

val hourHeight = 128.dp
val eventWidth = 250.dp

@OptIn(ExperimentalTime::class)
@Composable
fun Agenda(

) = Column {
    val today = Date()
    val todayShortDate = ShortDate.fromDate(today)
    val selectedDateIndex = remember {
        mutableStateOf(0)
    }

    Text(
        modifier = Modifier.padding(top = 16.dp, start = 32.dp, end = 32.dp, bottom = 16.dp),
        text = today.toLongString(),
        style = MaterialTheme.typography.h5
    )

    LazyRow(
        modifier = Modifier.padding(bottom = 32.dp),
        contentPadding = PaddingValues(horizontal = 32.dp)
    ) {

        items(182){ offset ->
            DateSelectorItem(
                date = ShortDate.fromDate(today.offset(Duration.days(offset))),
                today = todayShortDate,
                isSelected = offset == selectedDateIndex.value
            ){
                selectedDateIndex.value = offset
            }
        }
    }

    Schedule(
        modifier = Modifier.weight(1f),
        listOf(
            AgendaItem("Evento ejemplo", ShortDate.fromDate(today), HourRange.parse("12:00-13:30").first(), "Hola ramiroestradag@gmail.com"),
            AgendaItem("Evento ejemplo2", ShortDate.fromDate(today), HourRange.parse("12:30-15:30").first()),
            AgendaItem("Evento ejemplo3 asdasdasd", ShortDate.fromDate(today), HourRange.parse("12:00-17:30").first()),
        )
    )
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
            val hourRange = remember {
                mutableStateOf(IntRange(11, 24))
            }

            HourColumn(hourRange.value)
            Box(
                modifier = Modifier
                    .horizontalScroll(xScrollState)
                    .focusable(false)
            ){
                EventsContainer(hourRange.value, rearrangeList(events))
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
    elevation = 0.dp,
    backgroundColor = if(agendaItem.classSchedule != null) Color(agendaItem.classSchedule.color) else MaterialTheme.colors.surface
) {
    Column(
        Modifier.padding(16.dp)
    ) {
        Text(
            text = agendaItem.eventName,
            style = MaterialTheme.typography.h5,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            modifier = Modifier,
            text = agendaItem.description ?: ""
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