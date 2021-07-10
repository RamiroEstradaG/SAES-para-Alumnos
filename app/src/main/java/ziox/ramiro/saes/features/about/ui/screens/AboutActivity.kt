package ziox.ramiro.saes.features.about.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Dangerous
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.R
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.utils.launchUrl

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SAESParaAlumnosTheme {
                Scaffold(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp)
                    ) {
                        AboutItem(
                            leading = rememberVectorPainter(image = Icons.Rounded.Warning),
                            text = "La aplicación no está asociada al Instituto Politécnico Nacional (IPN) ni a sus unidades académicas. El desarrollo es independiente y está siendo manejado por estudiantes del IPN.",
                            backgroundColor = getCurrentTheme().danger,
                            contentColor = MaterialTheme.colors.onPrimary
                        )
                        AboutItem(
                            leading = painterResource(id = R.drawable.ic_octicons_mark_github),
                            text = "Código fuente",
                            isHighEmphasis = true,
                            backgroundColor = Color(0xFF24292e),
                            contentColor = Color.White
                        ) {
                            launchUrl("https://github.com/RamiroEstradaG/SAES-para-Alumnos")
                        }
                        AboutItem(
                            leading = painterResource(id = R.drawable.ic_trello_mark_white),
                            text = "Lista de tareas",
                            isHighEmphasis = true,
                            backgroundColor = Color(0xFF0079BF),
                            contentColor = Color.White
                        ) {
                            launchUrl("https://trello.com/b/bYPns3O2")
                        }
                        AboutItem(leading = rememberVectorPainter(image = Icons.Rounded.BugReport), text = "Reportar un bug") {
                            launchUrl("https://github.com/RamiroEstradaG/SAES-para-Alumnos/issues/new?labels=bug&template=issue.md&title=%5BFECHA+EN+YY-MM-DD%5D%3A+%5BTITULO+DEL+ISSUE%5D")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AboutItem(
    leading: Painter,
    text: String,
    isHighEmphasis : Boolean = false,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = getCurrentTheme().primaryText,
    onClick: (() -> Unit)? = null
) = Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp)
        .clip(MaterialTheme.shapes.medium)
        .clickable(
            interactionSource = MutableInteractionSource(),
            enabled = onClick != null,
            indication = rememberRipple(),
            onClick = {
                onClick?.invoke()
            }
        ),
    backgroundColor = backgroundColor
) {
    Row(
        modifier = Modifier.padding(if (isHighEmphasis) 16.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(if(isHighEmphasis) 32.dp else 24.dp),
            painter = leading,
            contentDescription = "About item icon",
            tint = contentColor
        )
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = text,
            style = if (isHighEmphasis) MaterialTheme.typography.h5 else MaterialTheme.typography.subtitle2,
            color = contentColor
        )
    }
}