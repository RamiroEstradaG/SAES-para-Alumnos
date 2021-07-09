package ziox.ramiro.saes.features.saes.ui.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flaky
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import coil.request.ImageRequest
import com.google.accompanist.coil.rememberCoilPainter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.filter
import ziox.ramiro.saes.features.saes.features.profile.view_models.ProfileState
import ziox.ramiro.saes.features.saes.features.profile.view_models.ProfileViewModel
import ziox.ramiro.saes.features.saes.view_models.MenuSection
import ziox.ramiro.saes.features.saes.view_models.SAESViewModel
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme

class BottomSheetDrawerModal : BottomSheetDialogFragment() {
    private val profileViewModel : ProfileViewModel by activityViewModels()
    private val saesViewModel: SAESViewModel by activityViewModels()

    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SAESParaAlumnosTheme {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ProfileHeader(profileState = profileViewModel.states.filter { it is ProfileState.UserComplete || it is ProfileState.UserLoading }.collectAsState(initial = null))
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                        ) {
                            ListItem(
                                modifier = Modifier.clickable {
                                    saesViewModel.changeSection(MenuSection.ETS)
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Flaky,
                                        contentDescription = "ETS"
                                    )
                                },
                                text = {
                                    Text(text = "ETS")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

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