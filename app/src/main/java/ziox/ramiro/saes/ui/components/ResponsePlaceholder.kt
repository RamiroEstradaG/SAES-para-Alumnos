package ziox.ramiro.saes.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.ui.theme.getCurrentTheme

@Composable
fun ResponsePlaceholder(
    painter: Painter,
    text: String,
) = Box(
    modifier = Modifier.fillMaxSize()
) {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .padding(bottom = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.size(200.dp),
            painter = painter,
            contentScale = ContentScale.Fit,
            contentDescription = "Image"
        )
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = text,
            style = MaterialTheme.typography.h5,
            color = getCurrentTheme().secondaryText,
            textAlign = TextAlign.Center
        )
    }
}