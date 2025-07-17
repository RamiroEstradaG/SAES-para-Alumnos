package ziox.ramiro.saes.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
fun SplashScreen(
    modifier: Modifier = Modifier
) = Box(
    modifier = modifier.fillMaxSize(),
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
            style = MaterialTheme.typography.headlineMedium
        )
    }
}