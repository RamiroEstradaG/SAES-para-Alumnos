package ziox.ramiro.saes.features.saes.features.kardex.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.UnfoldLess
import androidx.compose.material.icons.rounded.UnfoldMore
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
import ziox.ramiro.saes.features.saes.features.kardex.view_models.KardexViewModel
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.ui.theme.getCurrentTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Kardex(
    kardexViewModel: KardexViewModel = viewModel(
        factory = viewModelFactory { KardexViewModel(KardexWebViewRepository(LocalContext.current)) }
    )
) {
    val isExpanded = remember {
        mutableStateOf(false)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(bottom = 64.dp),
                onClick = {
                    isExpanded.value = !isExpanded.value
                }
            ) {
                Icon(
                    imageVector = if(isExpanded.value) Icons.Rounded.UnfoldLess else Icons.Rounded.UnfoldMore,
                    contentDescription = "Unfold icon"
                )
            }
        }
    ) {
        Crossfade(targetState = kardexViewModel.kardexData.value) {
            if (it != null){
                Column(
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
                            text = it.generalScore?.toString() ?: "-",
                            style = MaterialTheme.typography.h3,
                            color = gradeColor(it.generalScore?.toInt())
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .padding(top = 32.dp),
                        contentPadding = PaddingValues(bottom = 132.dp)
                    ) {
                        items(it.kardexPeriods){ period ->
                            val isExpandedSingle = remember {
                                mutableStateOf(false)
                            }

                            val expandableState = isExpanded.value xor isExpandedSingle.value

                            Column {
                                ListItem(
                                    modifier = Modifier.clickable {
                                        isExpandedSingle.value = !isExpandedSingle.value
                                    },
                                    text = {
                                        Text(
                                            modifier = Modifier.padding(start = 16.dp),
                                            text = period.periodName,
                                            style = MaterialTheme.typography.h5,
                                            color = if(expandableState) MaterialTheme.colors.primary else getCurrentTheme().primaryText
                                        )
                                    },
                                    trailing = {
                                        Icon(
                                            modifier = Modifier
                                                .padding(end = 8.dp)
                                                .size(32.dp),
                                            imageVector = if (expandableState) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                            contentDescription = "Expand",
                                            tint = if(expandableState) MaterialTheme.colors.primary else getCurrentTheme().primaryText
                                        )
                                    }
                                )
                                Column(
                                    modifier = Modifier
                                        .animateContentSize()
                                        .height(if (expandableState) Dp.Unspecified else 0.dp)
                                ) {
                                    period.kardexClasses.forEach {
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
    ErrorSnackbar(kardexViewModel.error)
}