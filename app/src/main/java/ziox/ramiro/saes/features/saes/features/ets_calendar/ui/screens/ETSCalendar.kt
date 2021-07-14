package ziox.ramiro.saes.features.saes.features.ets_calendar.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Filter
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ziox.ramiro.saes.R
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.saes.features.ets.view_models.ETSState
import ziox.ramiro.saes.features.saes.features.ets_calendar.data.repositories.ETSCalendarWebViewRepository
import ziox.ramiro.saes.features.saes.features.ets_calendar.view_models.ETSCalendarState
import ziox.ramiro.saes.features.saes.features.ets_calendar.view_models.ETSCalendarViewModel
import ziox.ramiro.saes.features.saes.ui.components.FilterBottomSheet
import ziox.ramiro.saes.ui.components.ResponsePlaceholder

@OptIn(ExperimentalMaterialApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun ETSCalendar(

) {
    val repository = ETSCalendarWebViewRepository(LocalContext.current)
    val etsCalendarViewModel: ETSCalendarViewModel = viewModel(
        factory = viewModelFactory { ETSCalendarViewModel(repository) }
    )

    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    BottomSheetScaffold(
        sheetContent = {
            FilterBottomSheet(etsCalendarViewModel)
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(bottom = 90.dp),
                onClick = {
                    coroutineScope.launch {
                        scaffoldState.bottomSheetState.expand()
                    }
                }
            ) {
                Icon(imageVector = Icons.Rounded.FilterAlt, contentDescription = "Filter")
            }
        },
        scaffoldState = scaffoldState
    ) {
        when(val state = etsCalendarViewModel.filterStates(ETSCalendarState.EventsComplete::class, ETSCalendarState.EventsLoading::class).value){
            is ETSCalendarState.EventsComplete -> if(state.events.isNotEmpty()){
                LazyColumn {
                    items(state.events.size){ i ->
                        Text(text = state.events[i].className)
                    }
                }
            }else{
                ResponsePlaceholder(
                    painter = painterResource(id = R.drawable.logging_off),
                    text = "No hay ETS disponibles con los campos seleccionados"
                )
            }
            is ETSState.ETSLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        repository.DebugView()
    }
}

