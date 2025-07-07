package ziox.ramiro.saes.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import ziox.ramiro.saes.ui.theme.getCurrentTheme

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ErrorSnackbar(
    errorState: Flow<String?>
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
            containerColor = getCurrentTheme().danger,
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
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = error.value ?: "",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun InfoSnackbar(
    infoState: Flow<String?>
) {
    val info = infoState.collectAsState(initial = null)

    AnimatedVisibility(
        visible = info.value != null,
        enter = slideInVertically(
            initialOffsetY = { -it }
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it }
        )
    ) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            containerColor = getCurrentTheme().info,
            shape = MaterialTheme.shapes.medium,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(28.dp),
                    imageVector = Icons.Rounded.Info,
                    contentDescription = "Icono",
                )
                Column {
                    Text(
                        text = "Información",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = info.value ?: "",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}