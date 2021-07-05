package ziox.ramiro.saes.ui.theme

import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

object LightTheme : ComposableTheme {
    override val primaryText = Color(0xFF292929)
    override val secondaryText = Color(0xFF575757)
    override val dangerColor = Color.Red
    override val toolbarColor = primaryColor.s500
    override val surfaceElevation = object : ColorElevation {
        override val e0 = Color(0xFFFFFFFF)
        override val e1 = Color(0xFFFFFFFF)
        override val e2 = Color(0xFFFFFFFF)
        override val e3 = Color(0xFFFFFFFF)
        override val e4 = Color(0xFFFFFFFF)
        override val e6 = Color(0xFFFFFFFF)
        override val e8 = Color(0xFFFFFFFF)
        override val e12 = Color(0xFFFFFFFF)
        override val e16 = Color(0xFFFFFFFF)
        override val e24 = Color(0xFFFFFFFF)
    }
    override val colors = lightColors(
        primary = primaryColor.s500,
        primaryVariant = primaryColor.s700,
        secondary = secondaryColor.s500,
        secondaryVariant = secondaryColor.s700,
        background = Color(0xFFEDF0F3),
        onBackground = primaryText,
        surface = Color.White,
        onSurface = primaryText,
        error = Color(0xFFB00020),
        onError = Color.White,
        onPrimary = Color.White,
        onSecondary = Color.White
    )
}