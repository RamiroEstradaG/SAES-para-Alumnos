package ziox.ramiro.saes.features.saes.features.profile.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.request.ImageRequest
import com.google.accompanist.coil.rememberCoilPainter
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.saes.features.profile.data.models.QRCodeScannerContract
import ziox.ramiro.saes.features.saes.features.profile.data.models.ProfileUser
import ziox.ramiro.saes.features.saes.features.profile.data.repositories.ProfileWebViewRepository
import ziox.ramiro.saes.features.saes.features.profile.ui.components.BarcodeCode39
import ziox.ramiro.saes.features.saes.features.profile.ui.components.QRCode
import ziox.ramiro.saes.features.saes.features.profile.view_models.ProfileViewModel
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.utils.*


@Composable
fun Profile(
    profileViewModel: ProfileViewModel = viewModel(
        factory = viewModelFactory { ProfileViewModel(ProfileWebViewRepository(LocalContext.current)) }
    )
) {
    if(profileViewModel.profile.value != null){
        profileViewModel.profile.value?.let {
            Scaffold(
                topBar = {
                    ProfileAppBar(profileUser = it)
                }
            ) { _ ->
                Box(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Column(
                        Modifier.padding(
                            start = 32.dp,
                            end = 32.dp,
                            top = 16.dp,
                            bottom = 64.dp
                        )
                    ) {
                        Text(
                            modifier = Modifier.padding(bottom = 8.dp, top = 16.dp),
                            text = "Datos generales",
                            style = MaterialTheme.typography.subtitle2
                        )
                        ProfileDataItem(Icons.Rounded.Cake, it.birthday.toLongString())
                        ProfileDataItem(Icons.Rounded.LocationCity,  "${it.state}, ${it.nationality}")
                        ProfileDataItem(Icons.Rounded.CorporateFare, it.school)
                        ProfileDataItem(Icons.Rounded.Fingerprint, it.curp)
                        ProfileDataItem(Icons.Rounded.MarkunreadMailbox, it.address.joinToString())
                        Text(
                            modifier = Modifier.padding(bottom = 8.dp, top = 16.dp),
                            text = "Datos de contacto",
                            style = MaterialTheme.typography.subtitle2
                        )
                        ProfileDataItem(Icons.Rounded.AlternateEmail, it.contactInformation.email)
                        ProfileDataItem(Icons.Rounded.Smartphone, it.contactInformation.mobilePhoneNumber)
                        ProfileDataItem(Icons.Rounded.Phone, it.contactInformation.phoneNumber)
                        ProfileDataItem(Icons.Rounded.Apartment, it.contactInformation.officePhone)
                        Text(
                            modifier = Modifier.padding(bottom = 8.dp, top = 16.dp),
                            text = "Datos escolares",
                            style = MaterialTheme.typography.subtitle2
                        )
                        ProfileDataItem(Icons.Rounded.CorporateFare, it.education.highSchoolName)
                        ProfileDataItem(Icons.Rounded.LocationCity, it.education.highSchoolState)
                        ProfileDataItem(Icons.Rounded.Grading, it.education.highSchoolFinalGrade.toString())
                        Text(
                            modifier = Modifier.padding(bottom = 8.dp, top = 16.dp),
                            text = "Progenitores o tutor",
                            style = MaterialTheme.typography.subtitle2
                        )
                        if(it.parent.guardianName.isNotBlank()){
                            ProfileDataItem(Icons.Rounded.EscalatorWarning, it.parent.guardianName)
                            ProfileDataItem(Icons.Rounded.Fingerprint, it.parent.guardianRfc)
                        }
                        if(it.parent.motherName.isNotBlank()){
                            ProfileDataItem(Icons.Rounded.EscalatorWarning, it.parent.motherName)
                        }
                        if(it.parent.fatherName.isNotBlank()){
                            ProfileDataItem(Icons.Rounded.EscalatorWarning, it.parent.fatherName)
                        }
                    }
                }
            }
        }
    }else{
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

    ErrorSnackbar(profileViewModel.error)
}


@Composable
fun ProfileAppBar(
    profileUser: ProfileUser
) = Card(
    modifier = Modifier
        .height(280.dp)
        .fillMaxWidth()
        .animateContentSize(),
    shape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = 32.dp,
        bottomEnd = 32.dp
    ),
    backgroundColor = MaterialTheme.colors.surface,
    elevation = 0.dp
) {
    Box(
        modifier = Modifier.padding(16.dp)
    ) {
        val isIdCardVisible = remember {
            mutableStateOf(false)
        }

        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = {
                isIdCardVisible.value = !isIdCardVisible.value
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.Fingerprint,
                contentDescription = "Card icon"
            )
        }

        Crossfade(targetState = isIdCardVisible.value) {
            if (!it){
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(150.dp),
                        painter = rememberCoilPainter(
                            request = ImageRequest
                                .Builder(LocalContext.current)
                                .data(profileUser.profilePicture.url)
                                .headers(profileUser.profilePicture.headers).build()),
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop
                    )

                    Text(
                        modifier = Modifier.padding(top = 24.dp),
                        text = profileUser.name,
                        style = MaterialTheme.typography.h5
                    )

                    Text(
                        text = profileUser.id,
                        style = MaterialTheme.typography.subtitle1
                    )
                }
            }else{
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    QRCode()
                    BarcodeCode39(profileUser.id)
                }
            }
        }
    }
}

@Composable
fun ProfileDataItem(
    icon: ImageVector,
    value: String
) = Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Icon(
        imageVector = icon,
        contentDescription = "Profile item",
        tint = MaterialTheme.colors.primary
    )
    Text(
        modifier = Modifier.padding(start = 16.dp),
        text = value
    )
}