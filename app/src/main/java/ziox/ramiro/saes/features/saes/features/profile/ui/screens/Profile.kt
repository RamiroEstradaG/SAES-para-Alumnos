package ziox.ramiro.saes.features.saes.features.profile.ui.screens

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.CorporateFare
import androidx.compose.material.icons.rounded.EscalatorWarning
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Grading
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.MarkunreadMailbox
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.google.accompanist.coil.rememberCoilPainter
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.saes.features.profile.data.models.ProfileUser
import ziox.ramiro.saes.features.saes.features.profile.data.repositories.ProfileWebViewRepository
import ziox.ramiro.saes.features.saes.features.profile.ui.components.BarcodeCode39
import ziox.ramiro.saes.features.saes.features.profile.ui.components.QRCode
import ziox.ramiro.saes.features.saes.features.profile.view_models.ProfileViewModel
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.utils.toLongString
import kotlin.math.absoluteValue


@Composable
fun Profile(
    context: Context = LocalContext.current,
    profileViewModel: ProfileViewModel = viewModel(
        factory = viewModelFactory { ProfileViewModel(ProfileWebViewRepository(context)) }
    )
) {
    val headerHeight = remember {
        mutableStateOf(280.dp)
    }
    val scrollingState = rememberScrollState()
    val coroutine = rememberCoroutineScope()

    val mainScrollState = with(LocalDensity.current) {
        rememberScrollableState {
            if (it < 0 && headerHeight.value > 58.dp && scrollingState.value == 0) {
                if (headerHeight.value + it.toDp() >= 58.dp) {
                    headerHeight.value += it.toDp()
                } else {
                    headerHeight.value = 58.dp
                }
                coroutine.launch {
                    scrollingState.scrollTo(0)
                }
            } else if (it > 0 && headerHeight.value < 280.dp && scrollingState.value == 0) {
                if (headerHeight.value + it.toDp() <= 280.dp) {
                    headerHeight.value += it.toDp()
                } else {
                    headerHeight.value = 280.dp
                }
            } else {
                scrollingState.dispatchRawDelta(-it)
            }

            it
        }
    }


    if (profileViewModel.profile.value != null) {
        profileViewModel.profile.value?.let { profileUser ->
            Scaffold(
                modifier = Modifier.scrollable(mainScrollState, orientation = Orientation.Vertical),
                topBar = {
                    ProfileAppBar(
                        profileUser = profileUser,
                        headerHeight = headerHeight.value
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .verticalScroll(scrollingState)
                        .padding(paddingValues)
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
                            style = MaterialTheme.typography.titleMedium
                        )
                        ProfileDataItem(Icons.Rounded.Cake, profileUser.birthday.toLongString())
                        ProfileDataItem(
                            Icons.Rounded.LocationCity,
                            "${profileUser.state}, ${profileUser.nationality}"
                        )
                        ProfileDataItem(Icons.Rounded.CorporateFare, profileUser.school)
                        ProfileDataItem(Icons.Rounded.Fingerprint, profileUser.curp)
                        ProfileDataItem(
                            Icons.Rounded.MarkunreadMailbox,
                            profileUser.address.joinToString()
                        )
                        Text(
                            modifier = Modifier.padding(bottom = 8.dp, top = 16.dp),
                            text = "Datos de contacto",
                            style = MaterialTheme.typography.titleMedium
                        )
                        ProfileDataItem(
                            Icons.Rounded.AlternateEmail,
                            profileUser.contactInformation.email
                        )
                        ProfileDataItem(
                            Icons.Rounded.Smartphone,
                            profileUser.contactInformation.mobilePhoneNumber
                        )
                        ProfileDataItem(
                            Icons.Rounded.Phone,
                            profileUser.contactInformation.phoneNumber
                        )
                        ProfileDataItem(
                            Icons.Rounded.Apartment,
                            profileUser.contactInformation.officePhone
                        )
                        Text(
                            modifier = Modifier.padding(bottom = 8.dp, top = 16.dp),
                            text = "Datos escolares",
                            style = MaterialTheme.typography.titleMedium
                        )
                        ProfileDataItem(
                            Icons.Rounded.CorporateFare,
                            profileUser.education.highSchoolName
                        )
                        ProfileDataItem(
                            Icons.Rounded.LocationCity,
                            profileUser.education.highSchoolState
                        )
                        ProfileDataItem(
                            Icons.Rounded.Grading,
                            profileUser.education.highSchoolFinalGrade.toString()
                        )
                        Text(
                            modifier = Modifier.padding(bottom = 8.dp, top = 16.dp),
                            text = "Progenitores o tutor",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (profileUser.parent.guardianName.isNotBlank()) {
                            ProfileDataItem(
                                Icons.Rounded.EscalatorWarning,
                                profileUser.parent.guardianName
                            )
                            ProfileDataItem(
                                Icons.Rounded.Fingerprint,
                                profileUser.parent.guardianRfc
                            )
                        }
                        if (profileUser.parent.motherName.isNotBlank()) {
                            ProfileDataItem(
                                Icons.Rounded.EscalatorWarning,
                                profileUser.parent.motherName
                            )
                        }
                        if (profileUser.parent.fatherName.isNotBlank()) {
                            ProfileDataItem(
                                Icons.Rounded.EscalatorWarning,
                                profileUser.parent.fatherName
                            )
                        }
                    }
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

    ErrorSnackbar(profileViewModel.error)
}


@OptIn(ExperimentalCoilApi::class)
@Composable
fun ProfileAppBar(
    profileUser: ProfileUser,
    headerHeight: Dp = 280.dp
) {
    val t = ((headerHeight.value - 58.dp.value) / 222).absoluteValue
    val sqt: Float = t * t
    val percentageCollapsed = sqt / (2.0f * (sqt - t) + 1.0f)

    Card(
        modifier = Modifier
            .height(headerHeight)
            .fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomStart = 32.dp.times(percentageCollapsed),
            bottomEnd = 32.dp.times(percentageCollapsed)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box {
            Row(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .padding(top = 8.dp)
                    .alpha(1 - percentageCollapsed),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(42.dp),
                    painter = rememberImagePainter(
                        request = ImageRequest
                            .Builder(LocalContext.current)
                            .data(profileUser.profilePicture.url)
                            .headers(profileUser.profilePicture.headers).build()
                    ),
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier.padding(start = 16.dp),
                ) {
                    Text(
                        text = profileUser.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = profileUser.id,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .alpha(percentageCollapsed)
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
                    if (!it) {
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
                                        .headers(profileUser.profilePicture.headers).build(),
                                    fadeIn = true
                                ),
                                contentDescription = "Profile picture",
                                contentScale = ContentScale.Crop
                            )

                            Text(
                                modifier = Modifier.padding(top = 24.dp),
                                text = profileUser.name,
                                style = MaterialTheme.typography.headlineMedium
                            )

                            Text(
                                text = profileUser.id,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    } else {
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
        tint = MaterialTheme.colorScheme.primary
    )
    Text(
        modifier = Modifier.padding(start = 16.dp),
        text = value
    )
}