package ziox.ramiro.saes.features.saes.features.ets_calendar.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Preview
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import ziox.ramiro.saes.R
import ziox.ramiro.saes.features.saes.features.ets_calendar.data.models.ETSCalendarItem
import ziox.ramiro.saes.features.saes.features.ets_calendar.view_models.ETSCalendarViewModel
import ziox.ramiro.saes.features.saes.features.schedule.data.models.Hour
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ShortDate
import ziox.ramiro.saes.features.saes.ui.components.FilterBottomSheet
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.ui.components.ResponsePlaceholder
import ziox.ramiro.saes.ui.components.TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ETSCalendar(
    context: Context = LocalContext.current,
    etsCalendarViewModel: ETSCalendarViewModel = viewModel(
        factory = viewModelFactory { ETSCalendarViewModel(ETSCalendarWebViewRepository(context)) }
    )
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    BottomSheetScaffold(
        sheetContent = @Composable {
            FilterBottomSheet(etsCalendarViewModel)
        },
//        floatingActionButton = @Composable { TODO: Encontrar reemplazo para FAB en BottomSheetScaffold
//            FloatingActionButton(
//                modifier = Modifier.padding(bottom = 90.dp),
//                onClick = {
//                    coroutineScope.launch {
//                        scaffoldState.bottomSheetState.expand()
//                    }
//                }
//            ) {
//                Icon(imageVector = Icons.Rounded.FilterAlt, contentDescription = "Filter")
//            }
//        },
        scaffoldState = scaffoldState
    ) {
        if(etsCalendarViewModel.etsCalendar.value != null){
            etsCalendarViewModel.etsCalendar.value?.let {
                val groupedEvents = it.groupBy { item ->
                    item.date
                }

                if(groupedEvents.isNotEmpty()){
                    Box(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .padding(bottom = 64.dp)
                        ) {
                            groupedEvents.forEach { group ->
                                ETSCalendarDayGroup(date = group.key, groupList = group.value)
                            }
                        }
                    }
                }else{
                    Box(
                        modifier = Modifier.padding(32.dp)
                    ) {
                        ResponsePlaceholder(
                            painter = painterResource(id = R.drawable.logging_off),
                            text = "No hay ETS disponibles con los campos seleccionados"
                        )
                    }
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

    ErrorSnackbar(etsCalendarViewModel.error)
    ErrorSnackbar(etsCalendarViewModel.filterError)
}

@Composable
fun ETSCalendarDayGroup(
    date: ShortDate,
    groupList: List<ETSCalendarItem>
) = Column(
    modifier = Modifier.padding(bottom = 32.dp)
) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = date.toString(),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center
    )
    Column {
        groupList.groupBy {
            it.hour
        }.forEach {
            ETSCalendarHourGroup(hour = it.key, groupList = it.value)
        }
    }
}


@Composable
fun ETSCalendarHourGroup(
    hour: Hour,
    groupList: List<ETSCalendarItem>
) = Row(
    modifier = Modifier.padding(vertical = 4.dp)
) {
    val density = LocalDensity.current
    val height = remember {
        mutableStateOf(0.dp)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            .height(height.value)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ){
        Text(
            modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp),
            text = hour.toString(),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
    }
    Column(
        modifier = Modifier
            .clip(
            RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ).background(MaterialTheme.colorScheme.surfaceContainer)
            .onGloballyPositioned {
                with(density){
                    height.value = it.size.height.toDp()
                }
            }
    ) {
        groupList.forEach {
            val isDialogVisible = remember {
                mutableStateOf(false)
            }

            ListItem(
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.clickable {
                      isDialogVisible.value = true
                },
                headlineContent = {
                    Text(text = it.className)
                },
                trailingContent = {
                    Icon(imageVector = Icons.Rounded.Preview, contentDescription = "Preview icon")
                }
            )

            if(isDialogVisible.value){
                AlertDialog(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onDismissRequest = {
                        isDialogVisible.value = false
                    },
                    title = @Composable {
                        Text(
                            text = it.className,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    text = @Composable {
                        Column {
                            Text(
                                text = "Edificio",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = it.building,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                modifier = Modifier.padding(top = 16.dp),
                                text = "Salón",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = it.classroom,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            text = "Aceptar",
                        ) {
                            isDialogVisible.value = false
                        }
                    },
                )
            }
        }
    }


}