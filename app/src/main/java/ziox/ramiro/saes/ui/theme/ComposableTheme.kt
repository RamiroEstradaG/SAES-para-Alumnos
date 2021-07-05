package ziox.ramiro.saes.ui.theme

import androidx.compose.material.*
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
    override val s50 = Color(0xFFFFF8E1)
    override val s100 = Color(0xFFFFECB3)
    override val s200 = Color(0xFFFFE082)
    override val s300 = Color(0xFFFFD54F)
    override val s400 = Color(0xFFFFCA28)
    override val s500 = Color(0xFFFFC107)
    override val s600 = Color(0xFFFFB300)
    override val s700 = Color(0xFFFFA000)
    override val s800 = Color(0xFFFF8F00)
    override val s900 = Color(0xFFFF6F00)
}

interface ComposableTheme {
    val colors: Colors

    val primaryText: Color
    val secondaryText: Color

    val dangerColor: Color

    val toolbarColor: Color
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