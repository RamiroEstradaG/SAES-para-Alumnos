package ziox.ramiro.saes.features.saes.features.occupancy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ExperimentalGraphicsApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ziox.ramiro.saes.R
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.saes.features.occupancy.data.models.ClassOccupancy
import ziox.ramiro.saes.features.saes.features.occupancy.data.repositories.OccupancyWebViewRepository
import ziox.ramiro.saes.features.saes.features.occupancy.view_models.OccupancyViewModel
import ziox.ramiro.saes.features.saes.ui.components.FilterBottomSheet
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.ui.components.ResponsePlaceholder
import ziox.ramiro.saes.ui.theme.getCurrentTheme


@OptIn(ExperimentalMaterialApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun Occupancy(
    occupancyViewModel: OccupancyViewModel = viewModel(
        factory = viewModelFactory { OccupancyViewModel(OccupancyWebViewRepository(LocalContext.current)) }
    )
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    BottomSheetScaffold(
        sheetContent = {
            FilterBottomSheet(occupancyViewModel)
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
        if(occupancyViewModel.occupancyList.value != null){
            occupancyViewModel.occupancyList.value?.let {
                if(it.isNotEmpty()){
                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = 16.dp,
                            start = 32.dp,
                            end = 32.dp,
                            bottom = 90.dp
                        )
                    ) {
                        items(it){ occupancy ->
                            OccupancyItem(occupancy)
                        }
                    }
                }else{
                    Box(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        ResponsePlaceholder(
                            painter = painterResource(id = R.drawable.logging_off),
                            text = "No hay datos de ocupabilidad con los campos seleccionados"
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
    ErrorSnackbar(occupancyViewModel.error)
}

@OptIn(ExperimentalGraphicsApi::class)
@Composable
fun OccupancyItem(
    classOccupancy: ClassOccupancy
) = Card(
    modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .padding(top = 8.dp)
) {
    val progress = if(classOccupancy.maximumQuota != 0){
        classOccupancy.currentlySignedUp.div(classOccupancy.maximumQuota.toDouble()).toFloat()
    }else{
        1f
    }

    val progressColor = if (progress == 1f){
        getCurrentTheme().divider
    }else{
        Color.hsv(
            120 - progress.times(120),
            1f,
            1f,
            0.2f
        )
    }

    LinearProgressIndicator(
        modifier = Modifier.fillMaxSize(),
        progress = progress,
        color = progressColor,
        backgroundColor = Color.Transparent
    )
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            text = classOccupancy.className,
            style = MaterialTheme.typography.h5,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${classOccupancy.currentlySignedUp}/${classOccupancy.maximumQuota}",
            style = MaterialTheme.typography.h6
        )
    }
}