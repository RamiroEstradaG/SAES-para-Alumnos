package ziox.ramiro.saes.features.saes.features.ets.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Event
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ziox.ramiro.saes.features.saes.features.ets.data.models.ETS
import ziox.ramiro.saes.features.saes.features.ets.data.models.ETSScore
import ziox.ramiro.saes.features.saes.features.ets.view_models.ETSViewModel
import ziox.ramiro.saes.features.saes.features.home.ui.components.gradeColor
import ziox.ramiro.saes.features.saes.view_models.MenuSection
import ziox.ramiro.saes.features.saes.view_models.SAESViewModel
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.ui.components.OutlineButton

@Composable
fun ETS(
    etsViewModel: ETSViewModel = viewModel(),
    saesViewModel: SAESViewModel = viewModel()
) = Box(
    modifier = Modifier.verticalScroll(rememberScrollState())
) {
    Column(
        modifier = Modifier.padding(
            start = 32.dp,
            end = 32.dp,
            top = 32.dp,
            bottom = 64.dp
        )
    ) {
        Text(
            text = "ETS",
            style = MaterialTheme.typography.h4
        )
        
        Text(
            modifier = Modifier.padding(top = 32.dp),
            text = "Inscripción",
            style = MaterialTheme.typography.h5
        )

        if(etsViewModel.availableETS.value != null){
            Column(modifier = Modifier.padding(top = 16.dp)) {
                etsViewModel.availableETS.value?.forEach {
                    ETSItem(it, etsViewModel)
                }
            }
        }else{
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Text(
            modifier = Modifier.padding(top = 32.dp),
            text = "Calificaciones",
            style = MaterialTheme.typography.h5
        )

        if(etsViewModel.scores.value != null){
            Column(modifier = Modifier.padding(top = 16.dp)) {
                etsViewModel.scores.value?.forEach {
                    ETSScoreItem(it, etsViewModel.availableETS)
                }
            }
        }else{
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        OutlineButton(
            modifier = Modifier
                .padding(top = 32.dp)
                .align(Alignment.CenterHorizontally),
            text = "Calendario de ETS",
            icon = Icons.Rounded.Event
        ){
            saesViewModel.changeSection(MenuSection.ETS_CALENDAR)
        }
    }

    ErrorSnackbar(etsViewModel.error)
}


@Composable
fun ETSItem(
    ets: ETS,
    etsViewModel: ETSViewModel = viewModel()
) = Row(
    modifier = Modifier.padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Text(
        modifier = Modifier
            .weight(1f)
            .padding(end = 8.dp),
        text = ets.name,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.h6
    )
    OutlineButton(
        text = "Inscribir",
        enabled = false //TODO: Probar las inscripciones
    ){
        etsViewModel.enrollETS(ets.index)
    }
}

@Composable
fun ETSScoreItem(
    ets: ETSScore,
    etsState: State<List<ETS>?>
) = Row(
    modifier = Modifier.padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically
){
    val className: String = ets.className.ifBlank {
        val etsName = etsState.value?.firstOrNull { it.id == ets.id }?.name ?: ""

        etsName.ifBlank {
            ets.id
        }
    }

    Text(
        modifier = Modifier.weight(1f),
        text = className,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.h6
    )
    Text(
        text = ets.grade?.toString() ?: "-",
        color = gradeColor(grade = ets.grade),
        style = MaterialTheme.typography.h5
    )
}