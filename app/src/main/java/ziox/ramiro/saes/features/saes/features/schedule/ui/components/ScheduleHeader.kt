package ziox.ramiro.saes.features.saes.features.schedule.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.features.saes.features.schedule.data.models.WeekDay
import ziox.ramiro.saes.features.saes.features.schedule.ui.screens.hourWidth
import ziox.ramiro.saes.features.saes.features.schedule.ui.screens.today
import ziox.ramiro.saes.ui.theme.getCurrentTheme

@Composable
fun ScheduleHeader(
    selectedDayOfWeek: MutableState<WeekDay?> = mutableStateOf(null)
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
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = selectedDayOfWeek.value?.dayName ?: "",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = if(today == it) MaterialTheme.colorScheme.primary
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
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "L",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = if(today == WeekDay.MONDAY) MaterialTheme.colorScheme.primary
                else getCurrentTheme().primaryText
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "M",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = if(today == WeekDay.TUESDAY) MaterialTheme.colorScheme.primary
                else getCurrentTheme().primaryText
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "M",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = if(today == WeekDay.WEDNESDAY) MaterialTheme.colorScheme.primary
                else getCurrentTheme().primaryText
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "J",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = if(today == WeekDay.THURSDAY) MaterialTheme.colorScheme.primary
                else getCurrentTheme().primaryText
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "V",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = if(today == WeekDay.FRIDAY) MaterialTheme.colorScheme.primary
                else getCurrentTheme().primaryText
            )
        }
    }
}