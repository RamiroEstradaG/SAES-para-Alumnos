package ziox.ramiro.saes.ui.theme

import android.content.Context
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color

class LightTheme(context: Context) : ComposableTheme {
    override val primaryText = Color(0xFF292929)
    override val secondaryText = Color(0xFF575757)
    override val hintText = Color(0xFF959595)

    override val success = Color(0xFF4CAF50)
    override val info = Color(0xFF03A9F4)
    override val warning = Color(0xFFFFC107)
    override val danger = Color(0xFFF44336)

    override val toolbar = primaryColor.s500
    override val onToolbar = Color.White

    override val divider = Color(0xFFC6C6C6)

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

    override val colors = dynamicLightColorScheme(context)
}