package ziox.ramiro.saes.features.saes.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import ziox.ramiro.saes.features.saes.data.models.FilterState
import ziox.ramiro.saes.ui.components.OutlineButton

@Composable
fun FlexView(
    modifier: Modifier = Modifier,
    @FlexDirection
    direction: Int = FlexDirection.ROW,
    content: List<@Composable () -> Unit>
) = AndroidView(
    modifier = modifier,
    factory = {
        val flexbox = FlexboxLayout(it)
        flexbox.flexDirection = direction
        flexbox.flexWrap = FlexWrap.WRAP

        content.forEach { item ->
            flexbox.addView(ComposeView(it).apply {
                setContent {
                    item()
                }
            })
        }

        flexbox
    }
)