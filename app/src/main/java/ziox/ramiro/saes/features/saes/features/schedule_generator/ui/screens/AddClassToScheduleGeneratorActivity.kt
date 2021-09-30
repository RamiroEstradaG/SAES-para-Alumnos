package ziox.ramiro.saes.features.saes.features.schedule_generator.ui.screens

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import ziox.ramiro.saes.R
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassScheduleCollection
import ziox.ramiro.saes.features.saes.features.schedule_generator.data.models.AddClassToScheduleGeneratorContract
import ziox.ramiro.saes.features.saes.features.school_schedule.data.repositories.SchoolScheduleWebViewRepository
import ziox.ramiro.saes.features.saes.features.school_schedule.view_models.SchoolScheduleViewModel
import ziox.ramiro.saes.features.saes.ui.components.FilterBottomSheet
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.ui.components.ResponsePlaceholder
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme

class AddClassToScheduleGeneratorActivity : AppCompatActivity() {
    private val schoolScheduleViewModel: SchoolScheduleViewModel by viewModels {
        viewModelFactory { SchoolScheduleViewModel(SchoolScheduleWebViewRepository(this)) }
    }

    @OptIn(ExperimentalMaterial3Api::class)
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
//                    floatingActionButton = { //TODO: Encontrar reemplazo para FAB en BottomSheetScaffold
//                        FloatingActionButton(
//                            onClick = {
//                                scope.launch {
//                                    scaffoldState.bottomSheetState.expand()
//                                }
//                            }
//                        ) {
//                            Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search")
//                        }
//                    },
                    scaffoldState = scaffoldState,
                ) {
                    if (schoolScheduleViewModel.schoolSchedule.value != null){
                        schoolScheduleViewModel.schoolSchedule.value?.let {
                            val scheduleCollections = ClassScheduleCollection.fromClassScheduleList(it)
                            if(it.isNotEmpty()){
                                LazyColumn(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    contentPadding = PaddingValues(
                                        top = 16.dp, start = 32.dp, end = 32.dp, bottom = 64.dp
                                    ),

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
                ErrorSnackbar(schoolScheduleViewModel.error)
                ErrorSnackbar(schoolScheduleViewModel.filterError)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableClassItem(
    classSchedule: ClassSchedule,
    onClick: () -> Unit
) = Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
    elevation = CardDefaults.cardElevation(0.dp),
) {
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        headlineContent = {
            Text(
                text = classSchedule.className,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = classSchedule.teacherName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        overlineContent = {
            Text(text = classSchedule.group.uppercase())
        },
        trailingContent = {
            IconButton(
                onClick = onClick,
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreTime,
                    contentDescription = "Add class",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}