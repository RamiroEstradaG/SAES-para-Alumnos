package ziox.ramiro.saes.features.saes.features.school_schedule.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import ziox.ramiro.saes.R
import ziox.ramiro.saes.features.saes.features.schedule.data.models.WeekDay
import ziox.ramiro.saes.features.saes.features.schedule.ui.components.ScheduleHeader
import ziox.ramiro.saes.features.saes.features.schedule.ui.components.ScheduleWeekContainer
import ziox.ramiro.saes.features.saes.features.school_schedule.view_models.SchoolScheduleViewModel
import ziox.ramiro.saes.features.saes.ui.components.FilterBottomSheet
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.ui.components.ResponsePlaceholder


@OptIn(ExperimentalMaterialApi::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Composable
fun SchoolSchedule(
    schoolScheduleViewModel: SchoolScheduleViewModel = viewModel()
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutine = rememberCoroutineScope()

    BottomSheetScaffold(
        sheetContent = {
            FilterBottomSheet(filterViewModel = schoolScheduleViewModel)
        },
        scaffoldState = scaffoldState,
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(bottom = 80.dp),
                onClick = {
                    coroutine.launch {
                        scaffoldState.bottomSheetState.expand()
                    }
                }
            ) {
                Icon(imageVector = Icons.Rounded.FilterAlt, contentDescription = "Filter icon")
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
    ErrorSnackbar(listOf(schoolScheduleViewModel.error, schoolScheduleViewModel.filterError).merge())
}