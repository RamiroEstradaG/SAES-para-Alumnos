package ziox.ramiro.saes.features.saes.features.schedule_generator.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ziox.ramiro.saes.R
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassScheduleCollection
import ziox.ramiro.saes.features.saes.features.schedule_generator.models.reposotories.AddClassToScheduleGeneratorContract
import ziox.ramiro.saes.features.saes.features.school_schedule.data.repositories.SchoolScheduleWebViewRepository
import ziox.ramiro.saes.features.saes.features.school_schedule.view_models.SchoolScheduleViewModel
import ziox.ramiro.saes.features.saes.ui.components.FilterBottomSheet
import ziox.ramiro.saes.ui.components.ResponsePlaceholder
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme

class AddClassToScheduleGeneratorActivity : AppCompatActivity() {
    private val schoolScheduleViewModel: SchoolScheduleViewModel by viewModels {
        viewModelFactory { SchoolScheduleViewModel(SchoolScheduleWebViewRepository(this)) }
    }

    @OptIn(ExperimentalMaterialApi::class,
        androidx.compose.animation.ExperimentalAnimationApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SAESParaAlumnosTheme {
                val scaffoldState = rememberBottomSheetScaffoldState()
                val scope = rememberCoroutineScope()

                BottomSheetScaffold(
                    sheetContent = {
                        FilterBottomSheet(filterViewModel = schoolScheduleViewModel)
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                scope.launch {
                                    scaffoldState.bottomSheetState.expand()
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search")
                        }
                    },
                    scaffoldState = scaffoldState,
                ) {
                    if (schoolScheduleViewModel.schoolSchedule.value != null){
                        schoolScheduleViewModel.schoolSchedule.value?.let {
                            val scheduleCollections = ClassScheduleCollection.fromClassScheduleList(it)
                            if(it.isNotEmpty()){
                                LazyColumn(
                                    contentPadding = PaddingValues(
                                        top = 16.dp, start = 32.dp, end = 32.dp, bottom = 64.dp
                                    )
                                ) {
                                    items(scheduleCollections){ classSchedule ->
                                        SelectableClassItem(
                                            classSchedule.schedules.first()
                                        ) {
                                            setResult(RESULT_OK, intent.apply {
                                                putExtra(AddClassToScheduleGeneratorContract.INTENT_EXTRA_RESULT_ADD_CLASS, classSchedule)
                                            })
                                            finish()
                                        }
                                    }
                                }
                            }else{
                                ResponsePlaceholder(
                                    painter = painterResource(id = R.drawable.logging_off),
                                    text = "No hay clases con los filtros seleccionados"
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
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SelectableClassItem(
    classSchedule: ClassSchedule,
    onClick: () -> Unit
) = Card(
    modifier = Modifier
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.medium)
        .padding(bottom = 16.dp),
    elevation = 0.dp
) {
    ListItem(
        text = {
            Text(
                text = classSchedule.className,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        secondaryText = {
            Text(
                text = classSchedule.teacherName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        overlineText = {
            Text(text = classSchedule.group.uppercase())
        },
        trailing = {
            IconButton(
                onClick = onClick,
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreTime,
                    contentDescription = "Add class",
                    tint = MaterialTheme.colors.primary
                )
            }
        }
    )
}