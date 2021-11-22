package ziox.ramiro.saes.features.saes.features.occupancy.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ExperimentalGraphicsApi
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ziox.ramiro.saes.R
import ziox.ramiro.saes.features.saes.features.occupancy.data.models.ClassOccupancy
import ziox.ramiro.saes.features.saes.features.occupancy.view_models.OccupancyViewModel
import ziox.ramiro.saes.features.saes.ui.components.FilterBottomSheet
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.ui.components.ResponsePlaceholder
import ziox.ramiro.saes.ui.theme.getCurrentTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Occupancy(
    context: Context = LocalContext.current,
    occupancyViewModel: OccupancyViewModel = viewModel(
        factory = viewModelFactory { OccupancyViewModel(OccupancyWebViewRepository(context)) }
    )
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    BottomSheetScaffold(
        sheetContent = {
            FilterBottomSheet(occupancyViewModel)
        },
//        floatingActionButton = { //TODO: Encontrar reemplazo para FAB en BottomSheetScaffold
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
        if (occupancyViewModel.occupancyList.value != null) {
            occupancyViewModel.occupancyList.value?.let {
                if (it.isNotEmpty()) {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = 16.dp,
                            start = 32.dp,
                            end = 32.dp,
                            bottom = 148.dp
                        )
                    ) {
                        items(it) { occupancy ->
                            OccupancyItem(occupancy)
                        }
                    }
                } else {
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
        } else {
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
        .padding(top = 8.dp),
    shape = RoundedCornerShape(100)
) {
    val progress =
        if (classOccupancy.maximumQuota != 0 && classOccupancy.currentlySignedUp in 0..classOccupancy.maximumQuota) {
            classOccupancy.currentlySignedUp.div(classOccupancy.maximumQuota.toDouble()).toFloat()
        } else {
            1f
        }

    val progressColor = if (progress == 1f) {
        getCurrentTheme().divider
    } else {
        Color.hsv(
            120 - progress.times(120),
            1f,
            1f,
            0.2f
        )
    }

    Box {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = progressColor,
            trackColor = Color.Transparent,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
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
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${classOccupancy.currentlySignedUp}/${classOccupancy.maximumQuota}",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}