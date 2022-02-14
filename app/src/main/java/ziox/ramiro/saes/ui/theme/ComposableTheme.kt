package ziox.ramiro.saes.ui.theme

import androidx.compose.material.Colors
import androidx.compose.material.Typography
import androidx.compose.ui.graphics.Color

val primaryColor = object : ColorShade {
    override val s50 = Color(0xFFFFEBEE)
    override val s100 = Color(0xFFFFCDD2)
    override val s200 = Color(0xFFEF9A9A)
    override val s300 = Color(0xFFE57373)
    override val s400 = Color(0xFFEF5350)
    override val s500 = Color(0xFFF44336)
    override val s600 = Color(0xFFE53935)
    override val s700 = Color(0xFFD32F2F)
    override val s800 = Color(0xFFC62828)
    override val s900 = Color(0xFFB71C1C)
}

val secondaryColor = object : ColorShade {
    override val s50 = Color(0xFFfbf0db)
    override val s100 = Color(0xFFf5d9a6)
    override val s200 = Color(0xFFeec06b)
    override val s300 = Color(0xFFe7a72c)
    override val s400 = Color(0xFFf4a236)
    override val s500 = Color(0xFFf29323)
    override val s600 = Color(0xFFed8720)
    override val s700 = Color(0xFFe7791e)
    override val s800 = Color(0xFFe06a1c)
    override val s900 = Color(0xFFd65116)
}

interface ComposableTheme {
    val colors: Colors
    val typography: Typography

    val primaryText: Color
    val secondaryText: Color
    val hintText: Color

    val success: Color
    val info: Color
    val warning: Color
    val danger: Color

    val toolbar: Color
    val onToolbar: Color

    val divider: Color

    val surfaceElevation: ColorElevation
}

interface ColorShade {
    val s50 : Color
    val s100 : Color
    val s200 : Color
    val s300 : Color
    val s400 : Color
    val s500 : Color
    val s600 : Color
    val s700 : Color
    val s800 : Color
    val s900 : Color
}

interface ColorElevation {
    val e0: Color
    val e1: Color
    val e2: Color
    val e3: Color
    val e4: Color
    val e6: Color
    val e8: Color
    val e12: Color
    val e16: Color
    val e24: Color
}