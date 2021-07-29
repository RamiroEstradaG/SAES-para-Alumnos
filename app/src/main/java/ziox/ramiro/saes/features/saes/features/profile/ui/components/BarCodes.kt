package ziox.ramiro.saes.features.saes.features.profile.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
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
import ziox.ramiro.saes.features.saes.features.profile.data.models.QRCodeScannerContract
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.utils.BarcodeTypes
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.createBarcodeImage

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