package ziox.ramiro.saes.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.transition.Slide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import kotlin.random.Random

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ErrorSnackbar(
    errorState: SharedFlow<String?>
) {
    val error = errorState.collectAsState(initial = null)

    AnimatedVisibility(
        visible = error.value != null,
        enter = slideInVertically(
            initialOffsetY = { -it }
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it }
        )
    ) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            backgroundColor = getCurrentTheme().danger,
            shape = MaterialTheme.shapes.medium,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(28.dp),
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = "Icono",
                )
                Column {
                    Text(
                        text = "Error",
                        color = MaterialTheme.colors.onPrimary,
                        style = MaterialTheme.typography.h5
                    )
                    Text(
                        text = error.value ?: "",
                        color = MaterialTheme.colors.onPrimary
                    )
                }
            }
        }
    }
}