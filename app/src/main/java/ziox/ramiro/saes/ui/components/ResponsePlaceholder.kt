package ziox.ramiro.saes.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ziox.ramiro.saes.ui.theme.getCurrentTheme

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ResponsePlaceholder(
    painter: Painter,
    text: String,
) {
    val isVisible = remember {
        mutableStateOf(false)
    }
    AnimatedVisibility(
        visible = isVisible.value,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
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
                    style = MaterialTheme.typography.headlineMedium,
                    color = getCurrentTheme().secondaryText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    LaunchedEffect(text){
        delay(100)
        isVisible.value = true
    }
}