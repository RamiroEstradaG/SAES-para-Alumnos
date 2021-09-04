package ziox.ramiro.saes.features.settings.ui.screens

import android.Manifest
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.ModeNight
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collect
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.saes.features.agenda.ui.screens.SelectableOptions
import ziox.ramiro.saes.features.settings.view_models.PersonalSavedDataViewModel
import ziox.ramiro.saes.ui.components.AsyncButton
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.ui.components.InfoSnackbar
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.updateWidgets

class SettingsActivity : AppCompatActivity(){
    private val personalSavedDataViewModel: PersonalSavedDataViewModel by viewModels{
        viewModelFactory { PersonalSavedDataViewModel(this) }
    }

    private val permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if (it){
            personalSavedDataViewModel.downloadMyPersonalData()
        }else{
            personalSavedDataViewModel.error.value = "No hay permisos para guardar el archivo"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userPreferences = UserPreferences.invoke(this)

        setContent {
            SAESParaAlumnosTheme {
                Scaffold { paddingValues ->
                    val nightModeOptions = listOf(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Configuración",
                            style = MaterialTheme.typography.h4
                        )
                        SettingsSection("Sistema") {
                            SettingsItem(icon = Icons.Rounded.ModeNight, title = "Modo oscuro") {
                                val selectedUiMode = remember {
                                    mutableStateOf(AppCompatDelegate.getDefaultNightMode())
                                }
                                SelectableOptions(
                                    options = nightModeOptions,
                                    selectionState = selectedUiMode,
                                    deSelectValue = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                                    stringAdapter = {
                                        when(it){
                                            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> "Predeterminado del sistema"
                                            AppCompatDelegate.MODE_NIGHT_NO -> "Modo claro"
                                            AppCompatDelegate.MODE_NIGHT_YES -> "Modo oscuro"
                                            else -> ""
                                        }
                                    ) {
                                        userPreferences.setPreference(PreferenceKeys.DefaultNightMode, it ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                                        AppCompatDelegate.setDefaultNightMode(it ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                                    }
                                )

                                LaunchedEffect(key1 = selectedUiMode){
                                    snapshotFlow { selectedUiMode.value }.collect {
                                        runOnUiThread {
                                            userPreferences.setPreference(PreferenceKeys.DefaultNightMode, it)
                                            AppCompatDelegate.setDefaultNightMode(it)
                                        }
                                    }
                                }
                            }
                            SettingsSection("Widgets") {
                                val sliderValue = remember {
                                    mutableFloatStateOf(userPreferences.getPreference(PreferenceKeys.ScheduleWidgetLeveling, 0).toFloat())
                                }
                                SettingsItem(icon = Icons.Rounded.Tune, title = "Calibración del Widget \"Horario semanal\" (${sliderValue.value.toInt()})") {
                                    Slider(
                                        value = sliderValue.component1(),
                                        valueRange = -100f..100f,
                                        onValueChange = sliderValue.component2(),
                                        onValueChangeFinished = {
                                            userPreferences.setPreference(PreferenceKeys.ScheduleWidgetLeveling, sliderValue.floatValue.toInt())
                                            updateWidgets()
                                        }
                                    )
                                }
                            }
                            if(userPreferences.getPreference(PreferenceKeys.IsFirebaseEnabled, false)){
                                SettingsSection("Datos almacenados en la nube") {
                                    AsyncButton(
                                        text = "Descargar mis datos",
                                        icon = Icons.Rounded.CloudDownload,
                                        isLoading = personalSavedDataViewModel.isDownloading.value
                                    ) {
                                        permissionsLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    }
                                    AsyncButton(
                                        modifier = Modifier.padding(top = 8.dp),
                                        text = "Eliminar mis datos",
                                        icon = Icons.Rounded.CloudOff,
                                        isLoading = personalSavedDataViewModel.isDeleting.value
                                    ) {
                                        personalSavedDataViewModel.deleteMyPersonalData()
                                    }
                                }
                            }
                        }
                    }
                }
                InfoSnackbar(personalSavedDataViewModel.info)
                ErrorSnackbar(personalSavedDataViewModel.error)
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
        style = MaterialTheme.typography.headlineMedium,
        color = getCurrentTheme().secondaryText
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
            contentDescription = "Settings icon",
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
    }
    content()
}