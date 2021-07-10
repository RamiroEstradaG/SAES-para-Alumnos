package ziox.ramiro.saes.features.saes.features.kardex.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.saes.features.home.ui.components.gradeColor
import ziox.ramiro.saes.features.saes.features.kardex.data.repositories.KardexWebViewRepository
import ziox.ramiro.saes.features.saes.features.kardex.view_models.KardexState
import ziox.ramiro.saes.features.saes.features.kardex.view_models.KardexViewModel
import ziox.ramiro.saes.ui.theme.getCurrentTheme

@ExperimentalMaterialApi
@Composable
fun Kardex(
    kardexViewModel: KardexViewModel = viewModel(
        factory = viewModelFactory { KardexViewModel(KardexWebViewRepository(LocalContext.current)) }
    )
) = Crossfade(targetState = kardexViewModel.statesAsState().value) {
    when(it){
        is KardexState.Complete -> Column(
            modifier = Modifier
                .fillMaxSize()

        ) {
            Column(
                modifier = Modifier.padding(
                    top = 16.dp,
                    start = 32.dp,
                    end = 32.dp
                )
            ) {
                Text(
                    text = "Promedio general",
                    style = MaterialTheme.typography.subtitle2
                )
                Text(
                    text = it.data.generalScore?.toString() ?: "-",
                    style = MaterialTheme.typography.h3,
                    color = gradeColor(it.data.generalScore?.toInt())
                )
            }

            LazyColumn(
                modifier = Modifier
                    .padding(top = 32.dp),
                contentPadding = PaddingValues(bottom = 64.dp)
            ) {
                items(it.data.kardexPeriods.size){ i ->
                    val isExpanded = remember {
                        mutableStateOf(false)
                    }
                    Column {
                        ListItem(
                            modifier = Modifier.clickable {
                                isExpanded.value = !isExpanded.value
                            },
                            text = {
                                Text(
                                    modifier = Modifier.padding(start = 16.dp),
                                    text = it.data.kardexPeriods[i].periodName,
                                    style = MaterialTheme.typography.h5,
                                    color = if(isExpanded.value) MaterialTheme.colors.primary else getCurrentTheme().primaryText
                                )
                            },
                            trailing = {
                                Icon(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(32.dp),
                                    imageVector = if (isExpanded.value) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                    contentDescription = "Expand",
                                    tint = if(isExpanded.value) MaterialTheme.colors.primary else getCurrentTheme().primaryText
                                )
                            }
                        )
                        Column(
                            modifier = Modifier
                                .animateContentSize()
                                .height(if (isExpanded.value) Dp.Unspecified else 0.dp)
                        ) {
                            it.data.kardexPeriods[i].kardexClasses.forEach {
                                ListItem(
                                    text = {
                                        Text(
                                            modifier = Modifier.padding(start = 16.dp),
                                            text = it.name,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    trailing = {
                                        Text(
                                            modifier = Modifier.padding(end = 16.dp),
                                            text = it.score?.toString() ?: "-",
                                            color = gradeColor(it.score),
                                            style = MaterialTheme.typography.h5
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        is KardexState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}