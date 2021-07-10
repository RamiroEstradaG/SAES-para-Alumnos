package ziox.ramiro.saes.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.Transition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ziox.ramiro.saes.ui.theme.getCurrentTheme

@SuppressLint("CoroutineCreationDuringComposition")
@ExperimentalAnimationApi
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
                    style = MaterialTheme.typography.h5,
                    color = getCurrentTheme().secondaryText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    rememberCoroutineScope().launch {
        delay(0)
        isVisible.value = true
    }
}