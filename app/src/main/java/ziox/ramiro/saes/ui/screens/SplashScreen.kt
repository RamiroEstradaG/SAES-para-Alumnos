package ziox.ramiro.saes.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.imageloading.rememberDrawablePainter
import ziox.ramiro.saes.R

@Composable
fun SplashScreen() = Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape),
            painter = rememberDrawablePainter(
                drawable = ContextCompat.getDrawable(
                    LocalContext.current,
                    R.mipmap.ic_launcher
                )
            ),
            contentDescription = "App logo"
        )
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "SAES para Alumnos",
            style = MaterialTheme.typography.h5
        )
    }
}