package ziox.ramiro.saes.features.saes.ui.components

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import coil.annotation.ExperimentalCoilApi
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import ziox.ramiro.saes.data.data_providers.rememberJsoupPainter
import ziox.ramiro.saes.features.about.ui.screens.AboutActivity
import ziox.ramiro.saes.features.saes.features.profile.data.models.ProfileUser
import ziox.ramiro.saes.features.saes.features.profile.view_models.ProfileViewModel
import ziox.ramiro.saes.features.saes.features.schedule_generator.ui.screens.ScheduleGeneratorActivity
import ziox.ramiro.saes.features.saes.view_models.MenuSection
import ziox.ramiro.saes.features.saes.view_models.SAESViewModel
import ziox.ramiro.saes.features.settings.ui.screens.SettingsActivity
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.isNetworkAvailable
import ziox.ramiro.saes.utils.launchUrl
import ziox.ramiro.saes.view_models.AuthViewModel


class BottomSheetDrawerModal : BottomSheetDialogFragment() {
    private val remoteConfig = Firebase.remoteConfig
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val saesViewModel: SAESViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SAESParaAlumnosTheme {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .verticalScroll(rememberScrollState())
                    ) {
                        ProfileHeader(profileState = profileViewModel.profile)
                        HorizontalDivider()
                        AndroidView(
                            factory = {
                                NestedScrollView(it)
                            }
                        ) {
                            it.addView(ComposeView(it.context).apply {
                                setContent {
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        MenuHeader(name = "Alumno")
                                        SectionMenuItem(section = MenuSection.KARDEX)
                                        SectionMenuItem(section = MenuSection.PERFORMANCE)
                                        SectionMenuItem(section = MenuSection.RE_REGISTRATION_APPOINTMENT)
                                        if (UserPreferences.invoke(requireContext()).getPreference(
                                                PreferenceKeys.IsFirebaseEnabled,
                                                false
                                            )
                                        ) {
                                            SectionMenuItem(section = MenuSection.AGENDA)
                                        }
                                        MenuHeader(name = "Académico")
                                        SectionMenuItem(section = MenuSection.ETS_CALENDAR)
                                        SectionMenuItem(section = MenuSection.SCHOOL_SCHEDULE)
                                        SectionMenuItem(section = MenuSection.OCCUPANCY)
                                        ActionMenuItem(
                                            icon = Icons.Rounded.MoreTime,
                                            name = "Generador de horario"
                                        ) {
                                            startActivity(
                                                Intent(
                                                    requireContext(),
                                                    ScheduleGeneratorActivity::class.java
                                                )
                                            )
                                        }
                                        if (
                                            remoteConfig.getString("calendario_escolarizado") != ""
                                            || remoteConfig.getString("calendario_no_escolarizado") != ""
                                        ) {
                                            MenuHeader(name = "Calendario académico")
                                        }
                                        if (remoteConfig.getString("calendario_escolarizado") != "") {
                                            ActionMenuItem(
                                                icon = Icons.Rounded.Event,
                                                name = "Calendario Modalidad Escolarizada"
                                            ) {
                                                context?.launchUrl(remoteConfig.getString("calendario_escolarizado"))
                                            }
                                        }
                                        if (remoteConfig.getString("calendario_no_escolarizado") != "") {
                                            ActionMenuItem(
                                                icon = Icons.Rounded.Event,
                                                name = "Calendario Modalidad No-Escolarizada"
                                            ) {
                                                context?.launchUrl(remoteConfig.getString("calendario_no_escolarizado"))
                                            }
                                        }
                                        MenuHeader(name = "Aplicación")
                                        ActionMenuItem(
                                            icon = Icons.Rounded.Settings,
                                            name = "Configuración"
                                        ) {
                                            startActivity(
                                                Intent(
                                                    requireContext(),
                                                    SettingsActivity::class.java
                                                )
                                            )
                                        }
                                        ActionMenuItem(
                                            icon = Icons.Rounded.Info,
                                            name = "Acerca de la aplicación"
                                        ) {
                                            startActivity(
                                                Intent(
                                                    requireContext(),
                                                    AboutActivity::class.java
                                                )
                                            )
                                        }
                                        ActionMenuItem(
                                            icon = Icons.Rounded.Logout,
                                            name = "Cerrar sesión",
                                            contentColor = getCurrentTheme().danger
                                        ) {
                                            authViewModel.logout()
                                        }
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun SectionMenuItem(
        section: MenuSection
    ) {
        val currentSection =
            saesViewModel.currentSection.collectAsState(initial = SAESViewModel.SECTION_INITIAL)

        if (LocalContext.current.isNetworkAvailable() || section.supportsOfflineMode) {
            Box(
                Modifier.padding(
                    horizontal = 8.dp,
                    vertical = 4.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable {
                            saesViewModel.changeSection(section)
                            dismiss()
                        }
                        .background(
                            if (currentSection.value == section) MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.1f
                            ) else Color.Transparent
                        )
                        .height(43.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = section.name,
                        tint = if (currentSection.value == section) MaterialTheme.colorScheme.primary else getCurrentTheme().primaryText
                    )
                    Text(
                        modifier = Modifier.padding(start = 16.dp),
                        text = section.sectionName,
                        color = if (currentSection.value == section) MaterialTheme.colorScheme.primary else getCurrentTheme().primaryText,
                        fontWeight = if (currentSection.value == section) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }


    @Composable
    fun ActionMenuItem(
        icon: ImageVector,
        name: String,
        contentColor: Color? = null,
        action: () -> Unit
    ) {
        Box(
            Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable {
                        action()
                        dismiss()
                    }
                    .height(43.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    tint = contentColor ?: getCurrentTheme().primaryText
                )
                Text(
                    modifier = Modifier.padding(start = 16.dp),
                    text = name,
                    color = contentColor ?: getCurrentTheme().primaryText,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun MenuHeader(
    name: String
) = Text(
    modifier = Modifier.padding(top = 16.dp, start = 16.dp),
    text = name,
    style = MaterialTheme.typography.titleMedium,
    color = getCurrentTheme().secondaryText
)

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ProfileHeader(
    profileState: State<ProfileUser?>
) = Crossfade(targetState = profileState.value) { crossFadeState ->
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        if (crossFadeState != null) {
            profileState.value?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(48.dp),
                        painter = rememberJsoupPainter(
                            imageUrl = it.profilePicture.url,
                            headers = it.profilePicture.headers
                        ),
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Text(
                            text = it.id,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}