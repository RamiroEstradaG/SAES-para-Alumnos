package ziox.ramiro.saes.features.saes.features.schedule.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.utils.getInitials

@Composable
fun ScheduleClassView(
    isExpanded: Boolean = false,
    startHour: Int,
    classSchedule: ClassSchedule,
    hourHeight: Dp = 170.dp
) = Card(
    modifier = Modifier
        .padding(top = hourHeight.times((classSchedule.hour.start.toDouble() - startHour).toFloat()))
        .height(hourHeight.times(classSchedule.hour.duration.toFloat()))
        .fillMaxWidth(),
    backgroundColor = Color(classSchedule.color),
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