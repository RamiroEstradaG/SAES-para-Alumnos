package ziox.ramiro.saes.features.saes.features.school_schedule.ui.screens

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ziox.ramiro.saes.R
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.saes.features.schedule.data.models.WeekDay
import ziox.ramiro.saes.features.saes.features.schedule.ui.components.ScheduleHeader
import ziox.ramiro.saes.features.saes.features.schedule.ui.components.ScheduleWeekContainer
import ziox.ramiro.saes.features.saes.features.schedule_generator.ui.screens.ScheduleGeneratorActivity
import ziox.ramiro.saes.features.saes.features.school_schedule.data.repositories.SchoolScheduleWebViewRepository
import ziox.ramiro.saes.features.saes.features.school_schedule.view_models.SchoolScheduleViewModel
import ziox.ramiro.saes.features.saes.ui.components.FilterBottomSheet
import ziox.ramiro.saes.ui.components.ResponsePlaceholder

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun SchoolSchedule(
    schoolScheduleViewModel: SchoolScheduleViewModel = viewModel(
        factory = viewModelFactory {
            SchoolScheduleViewModel(SchoolScheduleWebViewRepository(LocalContext.current))
        }
    )
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutine = rememberCoroutineScope()
    val context = LocalContext.current

    BottomSheetScaffold(
        sheetContent = {
            FilterBottomSheet(filterViewModel = schoolScheduleViewModel)
        },
        scaffoldState = scaffoldState,
        floatingActionButton = {
            Column(
                modifier = Modifier.padding(bottom = 148.dp)
            ) {
                FloatingActionButton(
                    modifier = Modifier.padding(bottom = 8.dp),
                    onClick = {
                        context.startActivity(Intent(context, ScheduleGeneratorActivity::class.java))
                    }
                ) {
                    Icon(imageVector = Icons.Rounded.MoreTime, contentDescription = "Schedule generator icon")
                }
                FloatingActionButton(
                    onClick = {
                        coroutine.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Rounded.FilterAlt, contentDescription = "Filter icon")
                }
            }
        }
    ) {
        if (schoolScheduleViewModel.schoolSchedule.value != null){
            schoolScheduleViewModel.schoolSchedule.value?.let {
                if(it.isNotEmpty()){
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val selectedDayOfWeek: MutableState<WeekDay?> = remember {
                            mutableStateOf(null)
                        }

                        ScheduleHeader(
                            selectedDayOfWeek = selectedDayOfWeek
                        )
                        ScheduleWeekContainer(
                            classSchedules = it,
                            selectedDayOfWeek = selectedDayOfWeek
                        )
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