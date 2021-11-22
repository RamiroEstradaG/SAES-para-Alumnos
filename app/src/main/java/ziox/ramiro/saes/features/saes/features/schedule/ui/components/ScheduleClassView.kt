package ziox.ramiro.saes.features.saes.features.schedule.ui.components

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.data.repositories.StorageFirebaseRepository
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.features.edit_class.data.models.EditClassContract
import ziox.ramiro.saes.features.saes.features.schedule.view_models.ScheduleViewModel
import ziox.ramiro.saes.utils.getInitials

@Composable
fun ScheduleClassView(
    context: Context = LocalContext.current,
    scheduleViewModel: ScheduleViewModel = viewModel(
        factory = viewModelFactory {
            ScheduleViewModel(
                ScheduleWebViewRepository(context),
                LocalAppDatabase.invoke(context).customScheduleGeneratorRepository(),
                StorageFirebaseRepository()
            )
        }
    ),
    isExpanded: Boolean = false,
    startHour: Int,
    classSchedule: ClassSchedule,
    hourHeight: Dp = 170.dp,
    isClassActionsEnabled: Boolean,
) = Card(
    modifier = Modifier
        .offset(y = hourHeight.times((classSchedule.scheduleDayTime.start.toDouble() - startHour).toFloat()))
        .height(hourHeight.times(classSchedule.scheduleDayTime.duration.toFloat()))
        .fillMaxWidth(),
    colors = CardDefaults.cardColors(
        containerColor = Color(classSchedule.color.toULong())
    ),
    shape = MaterialTheme.shapes.small,
    elevation = CardDefaults.cardElevation(0.dp)
) {
    val editLauncher = rememberLauncherForActivityResult(
        contract = EditClassContract()
    ){
        if (it != null){
            scheduleViewModel.editClass(it)
        }
    }

    Crossfade(targetState = isExpanded) {
        if(it){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = classSchedule.className,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if(isClassActionsEnabled){
                        IconButton(
                            modifier = Modifier.size(32.dp),
                            onClick = {
                                editLauncher.launch(classSchedule)
                            }
                        ) {
                            Icon(imageVector = Icons.Rounded.Edit, contentDescription = "Edit", tint = Color.White)
                        }
                    }
                }
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = "Profesor/a",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = classSchedule.teacherName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.padding(top = 20.dp),
                ) {
                    Column {
                        Text(
                            text = "Edificio",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = classSchedule.building,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Column(
                        Modifier.padding(start = 32.dp)
                    ) {
                        Text(
                            text = "Salón",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = classSchedule.classroom,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
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
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    modifier = Modifier.padding(top = 12.dp),
                    text = classSchedule.building,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = classSchedule.classroom,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}