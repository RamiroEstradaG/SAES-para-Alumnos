package ziox.ramiro.saes.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.ui.theme.getCurrentTheme

@Composable
fun AsyncButton(
    modifier: Modifier = Modifier,
    text: String = "button",
    icon: ImageVector? = null,
    isHighEmphasis: Boolean = false,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = {
            if(!isLoading){
                onClick()
            }
        },
        shape = RoundedCornerShape(100),

    ) {
        if (!isLoading){
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(icon != null){
                    Icon(
                        modifier = Modifier.padding(end = 16.dp),
                        imageVector = icon,
                        contentDescription = "Button icon",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = text.uppercase(),
                    modifier = Modifier.padding(if (isHighEmphasis) 6.dp else 0.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }else{
            Box {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(if (isHighEmphasis) 6.dp else 0.dp)
                        .size(24.dp)
                )
            }
        }
    }
}


@Composable
fun BaseButton(
    modifier: Modifier = Modifier,
    text: String = "button",
    isHighEmphasis: Boolean = false,
    icon: ImageVector? = null,
    onClick: () -> Unit = {}
) = Button(
    modifier = modifier,
    onClick = onClick,
    shape = RoundedCornerShape(100),
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
    ),
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(icon != null){
            Icon(
                modifier = Modifier.padding(end = 16.dp),
                imageVector = icon,
                contentDescription = "Button icon",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(if (isHighEmphasis) 6.dp else 0.dp)
        )
    }
}


@Composable
fun TextButton(
    modifier: Modifier = Modifier,
    text: String = "button",
    textColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit = {}
) = androidx.compose.material3.TextButton(
    onClick = onClick,
    modifier = modifier,
) {
    Text(
        text = text.uppercase(),
        color = textColor
    )
}

@Composable
fun OutlineButton(
    modifier: Modifier = Modifier,
    text: String = "button",
    isHighEmphasis: Boolean = false,
    textColor: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    onClick: () -> Unit = {}
) = androidx.compose.material3.OutlinedButton(
    modifier = modifier,
    onClick = onClick,
    shape = RoundedCornerShape(100),
    colors = ButtonDefaults.outlinedButtonColors(
        containerColor = backgroundColor ?: Color.Transparent
    ),
    enabled = enabled,
    border = BorderStroke(1.dp, borderColor ?: getCurrentTheme().divider)
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(icon != null){
            Icon(
                modifier = Modifier.padding(end = 8.dp),
                imageVector = icon,
                contentDescription = "Button icon"
            )
        }
        Text(
            text = text.uppercase(),
            color = if(enabled) textColor else getCurrentTheme().hintText,
            modifier = Modifier.padding(if (isHighEmphasis) 6.dp else 0.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun BaseButtonPreview() = SAESParaAlumnosTheme {
    BaseButton()
}

@Preview(showBackground = true)
@Composable
fun AsyncButtonPreview() = SAESParaAlumnosTheme {
    AsyncButton(isLoading = true) {}
}

@Preview(showBackground = true)
@Composable
fun OutlineButtonPreview() = SAESParaAlumnosTheme {
    OutlineButton(
        icon = Icons.Rounded.Event
    )
}

@Preview(showBackground = true)
@Composable
fun TextButtonPreview() = SAESParaAlumnosTheme {
    TextButton()
}