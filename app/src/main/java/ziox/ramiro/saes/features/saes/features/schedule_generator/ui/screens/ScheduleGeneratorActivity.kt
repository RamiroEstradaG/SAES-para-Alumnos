package ziox.ramiro.saes.features.saes.features.schedule_generator.ui.screens

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Preview
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dagger.hilt.android.AndroidEntryPoint
import ziox.ramiro.saes.R
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassScheduleCollection
import ziox.ramiro.saes.features.saes.features.schedule.data.models.WeekDay
import ziox.ramiro.saes.features.saes.features.schedule.ui.components.ScheduleHeader
import ziox.ramiro.saes.features.saes.features.schedule.ui.components.ScheduleWeekContainer
import ziox.ramiro.saes.features.saes.features.schedule_generator.data.models.AddClassToScheduleGeneratorContract
import ziox.ramiro.saes.features.saes.features.schedule_generator.view_models.ScheduleGeneratorViewModel
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.ui.components.ResponsePlaceholder
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.utils.isNetworkAvailable

@AndroidEntryPoint
class ScheduleGeneratorActivity: AppCompatActivity() {
    private val scheduleGeneratorViewModel: ScheduleGeneratorViewModel by viewModels()

    private val addClassToScheduleGeneratorLauncher = registerForActivityResult(
        AddClassToScheduleGeneratorContract()
    ){
        if(it == null) return@registerForActivityResult

        scheduleGeneratorViewModel.addClassToGenerator(ClassScheduleCollection.toGeneratorClassScheduleList(it))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SAESParaAlumnosTheme {
                val showSchedulePreview = remember {
                    mutableStateOf(false)
                }
                Scaffold(
                    floatingActionButton = {
                        Column {
                            if (scheduleGeneratorViewModel.scheduleItems.value != null){
                                scheduleGeneratorViewModel.scheduleItems.value?.let {
                                    if(it.isNotEmpty()){
                                        FloatingActionButton(
                                            modifier = Modifier.padding(bottom = 8.dp),
                                            onClick = {
                                                showSchedulePreview.value = true
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Preview,
                                                contentDescription = "Schedule generator icon"
                                            )
                                        }
                                    }
                                }
                            }
                            if(LocalContext.current.isNetworkAvailable()){
                                FloatingActionButton(
                                    onClick = {
                                        addClassToScheduleGeneratorLauncher.launch(Unit)
                                    }
                                ) {
                                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Filter icon")
                                }
                            }
                        }
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier.padding(paddingValues).padding(vertical = 16.dp)
                    ){
                        Text(
                            modifier = Modifier.padding(horizontal = 32.dp),
                            text = "Generador de horario",
                            style = MaterialTheme.typography.headlineLarge
                        )

                        if(scheduleGeneratorViewModel.scheduleItems.value != null){
                            scheduleGeneratorViewModel.scheduleItems.value?.let {
                                if(it.isNotEmpty()){
                                    val classSchedules = it.map {schedule -> ClassSchedule.fromGeneratorClassSchedule(schedule) }
                                    val classCollections = ClassScheduleCollection.fromClassScheduleList(classSchedules)
                                    LazyColumn(
                                        modifier = Modifier.padding(top = 16.dp),
                                        contentPadding = PaddingValues(
                                            horizontal = 32.dp,
                                            vertical = 16.dp
                                        )
                                    ) {
                                        items(classCollections){ item ->
                                            ScheduleGeneratorItem(item, scheduleGeneratorViewModel)
                                        }
                                    }

                                    if(showSchedulePreview.value){
                                        Dialog(
                                            onDismissRequest = {
                                                showSchedulePreview.value = false
                                            }
                                        ) {
                                            val selectedWeekDay = remember {
                                                mutableStateOf<WeekDay?>(null)
                                            }
                                            Card {
                                                Column {
                                                    ScheduleHeader(selectedWeekDay)
                                                    ScheduleWeekContainer(
                                                        classSchedules,
                                                        selectedWeekDay,
                                                        isClassActionsEnabled = false
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }else{
                                    ResponsePlaceholder(
                                        painter = painterResource(id = R.drawable.logging_off),
                                        text = "No has agregado clases al generador"
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
                    ErrorSnackbar(scheduleGeneratorViewModel.error)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleGeneratorItem(
    generatorClassSchedule: ClassScheduleCollection,
    scheduleGeneratorViewModel: ScheduleGeneratorViewModel
) = Card(
    modifier = Modifier.padding(bottom = 16.dp),
    elevation = CardDefaults.cardElevation(0.dp),
) {
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        headlineContent = {
            Text(
                text = generatorClassSchedule.className,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = generatorClassSchedule.teacherName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        overlineContent = {
            Text(text = generatorClassSchedule.group)
        },
        trailingContent = {
            IconButton(
                onClick = {
                    scheduleGeneratorViewModel.removeClass(generatorClassSchedule.classId)
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Remove item",
                    tint = getCurrentTheme().danger
                )
            }
        }
    )
}