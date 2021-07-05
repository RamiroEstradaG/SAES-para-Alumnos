package ziox.ramiro.saes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController


@Composable
fun getCurrentTheme(darkTheme: Boolean = isSystemInDarkTheme()) = if (darkTheme) DarkTheme else LightTheme

@Composable
fun SAESParaAlumnosTheme(content: @Composable () -> Unit) {
    val theme = getCurrentTheme()

    MaterialTheme(
        colors = theme.colors,
        typography = Typography(theme),
        shapes = Shapes
    ){
        rememberSystemUiController().setStatusBarColor(
            theme.colors.background,
            true
        )
        content()
    }
}