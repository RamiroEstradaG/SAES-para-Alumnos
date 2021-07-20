package ziox.ramiro.saes.features.saes.features.profile.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import ziox.ramiro.saes.features.saes.features.profile.view_models.ProfileViewModel
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.utils.BarcodeTypes
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.createBarcodeImage


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
            ) {

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
    )
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
fun QRCode() {
    val context = LocalContext.current

    val qrCodeUrl = remember {
        mutableStateOf(UserPreferences.invoke(context).getPreference(PreferenceKeys.QrUrl, ""))
    }

    val qrCodeScannerLauncher = rememberLauncherForActivityResult(contract = QRCodeScannerContract()) {
        if(it != null){
            UserPreferences.invoke(context).setPreference(PreferenceKeys.QrUrl, it)
            qrCodeUrl.value = it
        }
    }

    Box(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Color.White)
            .size(150.dp)
            .clickable(
                indication = rememberRipple(),
                interactionSource = MutableInteractionSource(),
                onClick = {
                    qrCodeScannerLauncher.launch(Unit)
                }
            ),
    ) {
        if(qrCodeUrl.value.isBlank()){
            Column(
                Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .fillMaxSize()
                    .border(1.dp, getCurrentTheme().divider, MaterialTheme.shapes.medium)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.QrCodeScanner,
                    contentDescription = "QR Scanner",
                    tint = Color.Black
                )
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "Agregar Código QR de la credencial",
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
            }
        }else{
            Image(
                modifier = Modifier.fillMaxSize(),
                bitmap = createBarcodeImage(qrCodeUrl.value, BarcodeTypes.QRCode, 100.dp, 100.dp).asImageBitmap(),
                contentDescription = "Code",
                contentScale = ContentScale.FillBounds
            )
        }
    }
}


@Composable
fun BarcodeCode39(data: String = "1234567890") = Column(
    modifier = Modifier
        .clip(MaterialTheme.shapes.medium)
        .background(Color.White)
        .fillMaxWidth()
        .padding(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Image(
        modifier = Modifier.fillMaxWidth(),
        bitmap = createBarcodeImage(data, BarcodeTypes.Barcode39, 270.dp, 30.dp).asImageBitmap(),
        contentDescription = "Code",
        contentScale = ContentScale.FillWidth
    )
    Text(
        text = data,
        style = MaterialTheme.typography.subtitle2,
        color = Color.Black
    )
}