package ziox.ramiro.saes.features.saes.features.ets_calendar.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Filter
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.saes.features.ets_calendar.data.repositories.ETSCalendarWebViewRepository
import ziox.ramiro.saes.features.saes.features.ets_calendar.view_models.ETSCalendarViewModel
import ziox.ramiro.saes.features.saes.ui.components.FilterBottomSheet

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ETSCalendar(
    etsCalendarViewModel: ETSCalendarViewModel = viewModel(
        factory = viewModelFactory { ETSCalendarViewModel(ETSCalendarWebViewRepository(LocalContext.current)) }
    )
) {
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

    }
}