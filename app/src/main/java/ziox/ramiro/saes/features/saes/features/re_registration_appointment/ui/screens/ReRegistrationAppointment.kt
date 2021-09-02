package ziox.ramiro.saes.features.saes.features.re_registration_appointment.ui.screens

import android.content.Intent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.saes.features.re_registration_appointment.data.repositories.ReRegistrationWebViewRepository
import ziox.ramiro.saes.features.saes.features.re_registration_appointment.view_models.ReRegistrationAppointmentViewModel
import ziox.ramiro.saes.features.saes.features.schedule_generator.ui.screens.ScheduleGeneratorActivity
import ziox.ramiro.saes.features.saes.ui.components.FlexView
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.ui.components.OutlineButton
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.utils.toLongString
import ziox.ramiro.saes.utils.toLongStringAndHour
import ziox.ramiro.saes.utils.toStringPrecision

@Composable
fun ReRegistrationAppointment(
    reRegistrationViewModel: ReRegistrationAppointmentViewModel = viewModel(
        factory = viewModelFactory { ReRegistrationAppointmentViewModel(ReRegistrationWebViewRepository(
            LocalContext.current)) }
    )
) = Crossfade(targetState = reRegistrationViewModel.reRegistrationData.value) {
    val context = LocalContext.current

    if(it != null){
        Box(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(top = 16.dp, start = 32.dp, end = 32.dp, bottom = 64.dp)
            ) {
                Text(
                    text = "Reinscripción",
                    style = MaterialTheme.typography.h4
                )
                Text(
                    text = it.appointmentDate?.toLongStringAndHour() ?: "Reinscripción no disponible",
                    style = MaterialTheme.typography.h6
                )
                if(it.appointmentDateExpiration != null){
                    Text(
                        text = """
                            Hasta
                            ${it.appointmentDateExpiration.toLongStringAndHour()}
                        """.trimIndent(),
                        style = MaterialTheme.typography.h6
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlineButton(
                        text = "Generador de horario",
                        icon = Icons.Rounded.MoreTime
                    ){
                        context.startActivity(Intent(context, ScheduleGeneratorActivity::class.java))
                    }
                }
                Row(
                    Modifier
                        .padding(top = 48.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CreditsElement(title = "Carga mínima", value = it.creditsMinimum.toString())
                    CreditsElement(title = "Carga media", value = it.creditsMedium.toString())
                    CreditsElement(title = "Carga máxima", value = it.creditsMaximum.toString())
                }
                ProgressChart(
                    modifier = Modifier.padding(top = 48.dp),
                    title = "Créditos",
                    max = it.creditsTotal,
                    elements = listOf(
                        ChartElement(
                            "Obtenidos",
                            it.creditsObtained,
                            MaterialTheme.colors.primary
                        )
                    )
                )
                ProgressChart(
                    modifier = Modifier.padding(top = 48.dp),
                    title = "Periodos escolares",
                    max = it.careerMaximumDuration.toDouble(),
                    elements = listOf(
                        ChartElement(
                            "Cursado",
                            it.careerCurrentDuration.toDouble(),
                            MaterialTheme.colors.primary
                        ),
                        ChartElement(
                            "Previsto",
                            it.careerMediumDuration.toDouble(),
                            MaterialTheme.colors.secondary
                        )
                    )
                ){ value ->
                    value.toInt().toString()
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

    ErrorSnackbar(reRegistrationViewModel.error)
}


@Composable
fun CreditsElement(
    title: String,
    value: String
) = Column(
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(
        text = title,
        style = MaterialTheme.typography.subtitle2
    )
    Text(
        text = value,
        style = MaterialTheme.typography.h5
    )
}

@Composable
fun ProgressChart(
    modifier: Modifier = Modifier,
    title: String,
    max: Double,
    elements: List<ChartElement>,
    valueFormatter: (Double) -> String = { it.toString() }
) = Column(
    modifier = modifier.fillMaxWidth()
) {
    Text(
        text = title,
        style = MaterialTheme.typography.subtitle2
    )
    Card(
        modifier = Modifier
            .height(70.dp)
            .padding(top = 8.dp),
        elevation = 0.dp,
        backgroundColor = getCurrentTheme().divider
    ) {
        elements.forEach {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(
                        max
                            .minus(it.value)
                            .toFloat()
                    ),
                color = it.color,
                progress = it.value.div(max).toFloat(),
                backgroundColor = Color.Transparent
            )
        }
    }
    FlexView(
        content = elements.map {
            {
                Row(
                    modifier = Modifier.padding(end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(it.color)
                    ) {}
                    Text(
                        modifier = Modifier.padding(start = 6.dp),
                        text = "${it.name} (${valueFormatter(it.value)} - ${it.value.div(max).times(100).toStringPrecision(1)}%)"
                    )
                }
            }
        }
    )
}

data class ChartElement(
    val name: String,
    val value: Double,
    val color: Color
)