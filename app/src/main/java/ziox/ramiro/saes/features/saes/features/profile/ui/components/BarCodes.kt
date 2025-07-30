package ziox.ramiro.saes.features.saes.features.profile.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
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

    val qrCodeScannerLauncher = rememberLauncherForActivityResult(contract = ScanContract()) {
        if(it != null && it.contents != null && it.contents.isNotBlank()) {
            UserPreferences.invoke(context).setPreference(PreferenceKeys.QrUrl, it.contents)
            qrCodeUrl.value = it.contents
        }
    }

    Box(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Color.White)
            .size(150.dp)
            .clickable(
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    qrCodeScannerLauncher.launch(
                        ScanOptions()
                            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                            .setBarcodeImageEnabled(true)
                            .setBeepEnabled(false)
                            .setOrientationLocked(false)
                            .setPrompt("Escanea el QR de tu credencial")
                    )
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
        style = MaterialTheme.typography.titleMedium,
        color = Color.Black
    )
}