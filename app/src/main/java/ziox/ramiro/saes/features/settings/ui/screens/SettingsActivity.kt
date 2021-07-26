package ziox.ramiro.saes.features.settings.ui.screens

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ModeNight
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.features.saes.features.agenda.ui.screens.SelectableOptions
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.toStringPrecision
import ziox.ramiro.saes.utils.updateWidgets

class SettingsActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userPreferences = UserPreferences.invoke(this)

        setContent {
            SAESParaAlumnosTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = "Configuración",
                            style = MaterialTheme.typography.h4
                        )
                        SettingsSection("Sistema") {
                            SettingsItem(icon = Icons.Rounded.ModeNight, title = "Modo oscuro") {
                                SelectableOptions(
                                    options = listOf("Predeterminado del sistema", "Modo claro", "Modo oscuro"),
                                    initialSelection = when(AppCompatDelegate.getDefaultNightMode()){
                                        AppCompatDelegate.MODE_NIGHT_NO -> 1
                                        AppCompatDelegate.MODE_NIGHT_YES -> 2
                                        else -> 0
                                    }
                                ) {
                                    userPreferences.setPreference(PreferenceKeys.DefaultNightMode, it ?: 0)
                                    AppCompatDelegate.setDefaultNightMode(when(it){
                                        1 -> AppCompatDelegate.MODE_NIGHT_NO
                                        2 -> AppCompatDelegate.MODE_NIGHT_YES
                                        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                                    })
                                }
                            }
                        }
                        SettingsSection("Widgets") {
                            val sliderValue = remember {
                                mutableStateOf(userPreferences.getPreference(PreferenceKeys.ScheduleWidgetLeveling, 0).toFloat())
                            }
                            SettingsItem(icon = Icons.Rounded.Tune, title = "Calibración del Widget \"Horario semanal\" (${sliderValue.value.toInt()})") {
                                Slider(
                                    value = sliderValue.component1(),
                                    valueRange = -100f..100f,
                                    onValueChange = sliderValue.component2(),
                                    onValueChangeFinished = {
                                        userPreferences.setPreference(PreferenceKeys.ScheduleWidgetLeveling, sliderValue.value.toInt())
                                        updateWidgets()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    sectionTitle: String,
    content: @Composable () -> Unit
) = Column {
    Text(
        modifier = Modifier.padding(bottom = 8.dp, top = 32.dp),
        text = sectionTitle,
        style = MaterialTheme.typography.h5,
        color = getCurrentTheme().secondaryText
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit
) = Column(
    modifier = Modifier.fillMaxWidth()
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(end = 8.dp),
            imageVector = icon,
            contentDescription = "Settings icon"
        )
        Text(
            text = title,
            style = MaterialTheme.typography.subtitle2
        )
    }
    content()
}