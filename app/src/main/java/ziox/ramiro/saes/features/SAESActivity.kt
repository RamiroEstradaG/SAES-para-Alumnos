package ziox.ramiro.saes.features

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme

class SAESActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SAESParaAlumnosTheme {

            }
        }
    }
}

