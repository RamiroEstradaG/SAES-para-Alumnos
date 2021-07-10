package ziox.ramiro.saes.features.saes.ui.components

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material.icons.rounded.Logout
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
import coil.request.ImageRequest
import com.google.accompanist.coil.rememberCoilPainter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ziox.ramiro.saes.features.about.ui.screens.AboutActivity
import ziox.ramiro.saes.features.saes.features.profile.view_models.ProfileState
import ziox.ramiro.saes.features.saes.features.profile.view_models.ProfileViewModel
import ziox.ramiro.saes.features.saes.view_models.MenuSection
import ziox.ramiro.saes.features.saes.view_models.SAESViewModel
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.utils.launchUrl
import ziox.ramiro.saes.view_models.AuthViewModel


class BottomSheetDrawerModal(
    private var profileViewModel: ProfileViewModel,
    private var saesViewModel: SAESViewModel,
    private var authViewModel: AuthViewModel
): BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SAESParaAlumnosTheme {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProfileHeader(profileState = profileViewModel.statesAsState())
                        Divider()
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                        ) {
                            MenuHeader(name = "Alumno")
                            SectionMenuItem(section = MenuSection.KARDEX, icon = Icons.Rounded.ListAlt, name = "Kárdex")
                            MenuHeader(name = "Calendario académico")
                            ActionMenuItem(icon = Icons.Rounded.Event, name = "Calendario Modalidad Escolarizada"){
                                context?.launchUrl("https://www.ipn.mx/assets/files/main/docs/inicio/cal-Escolarizada-21-22.pdf")
                            }
                            ActionMenuItem(icon = Icons.Rounded.Event, name = "Calendario Modalidad No-Escolarizada"){
                                context?.launchUrl("https://www.ipn.mx/assets/files/main/docs/inicio/cal-NoEscolarizada-21-22.pdf")
                            }
                            MenuHeader(name = "Aplicación")
                            ActionMenuItem(icon = Icons.Rounded.Info, name = "Acerca de la aplicación"){
                                startActivity(Intent(requireContext(), AboutActivity::class.java))
                            }
                            ActionMenuItem(icon = Icons.Rounded.Logout, name = "Cerrar sesión"){
                                authViewModel.logout()
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun SectionMenuItem(
        section: MenuSection,
        icon: ImageVector,
        name: String
    ) {
        val currentSection = saesViewModel.currentSection.collectAsState(initial = SAESViewModel.SECTION_INITIAL)

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
                        if (currentSection.value == section) MaterialTheme.colors.primary.copy(alpha = 0.2f) else Color.Transparent
                    )
                    .height(43.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = section.name,
                    tint = if (currentSection.value == section) MaterialTheme.colors.primary else getCurrentTheme().primaryText
                )
                Text(
                    modifier = Modifier.padding(start = 16.dp),
                    text = name,
                    color = if (currentSection.value == section) MaterialTheme.colors.primary else getCurrentTheme().primaryText,
                    fontWeight = if (currentSection.value == section) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }


    @Composable
    fun ActionMenuItem(
        icon: ImageVector,
        name: String,
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
                    tint = getCurrentTheme().primaryText
                )
                Text(
                    modifier = Modifier.padding(start = 16.dp),
                    text = name,
                    color = getCurrentTheme().primaryText,
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
    style = MaterialTheme.typography.subtitle2,
    color = getCurrentTheme().secondaryText
)

@Composable
fun ProfileHeader(
    profileState : State<ProfileState?>
) = Crossfade(targetState = profileState.value) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        when(it){
            is ProfileState.UserComplete -> Row(
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
                    painter = rememberCoilPainter(
                        request = ImageRequest
                            .Builder(LocalContext.current)
                            .data(it.userData.profilePicture.url)
                            .headers(it.userData.profilePicture.headers).build()),
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = it.userData.name,
                        style = MaterialTheme.typography.h5
                    )

                    Text(
                        text = it.userData.id,
                        style = MaterialTheme.typography.subtitle1
                    )
                }
            }
            is ProfileState.UserLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> Box {}
        }
    }
}