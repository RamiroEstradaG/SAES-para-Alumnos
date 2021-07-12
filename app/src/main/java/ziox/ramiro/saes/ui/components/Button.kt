package ziox.ramiro.saes.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Event
import androidx.compose.runtime.*
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
    isHighEmphasis: Boolean = false,
    isLoadingState: State<Boolean?> = remember { mutableStateOf(false) },
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = {
            if(isLoadingState.value != true){
                onClick()
            }
        },
        shape = RoundedCornerShape(100)
    ) {
        if (isLoadingState.value != true){
            Text(
                text = text.uppercase(),
                modifier = Modifier.padding(if (isHighEmphasis) 6.dp else 0.dp)
            )
        }else{
            Box {
                CircularProgressIndicator(
                    color = MaterialTheme.colors.onPrimary,
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
    onClick: () -> Unit = {}
) = Button(
    modifier = modifier,
    onClick = onClick,
    shape = RoundedCornerShape(100),
) {
    Text(
        text = text.uppercase(),
        modifier = Modifier.padding(if (isHighEmphasis) 6.dp else 0.dp)
    )
}


@Composable
fun TextButton(
    modifier: Modifier = Modifier,
    text: String = "button",
    textColor: Color = MaterialTheme.colors.primary,
    onClick: () -> Unit = {}
) = TextButton(
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
    textColor: Color = MaterialTheme.colors.primary,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    onClick: () -> Unit = {}
) = OutlinedButton(
    modifier = modifier,
    onClick = onClick,
    shape = RoundedCornerShape(100),
    colors = ButtonDefaults.outlinedButtonColors(
        backgroundColor = backgroundColor ?: Color.Transparent
    ),
    enabled = enabled,
    border = BorderStroke(1.dp, borderColor ?: Color.LightGray) // TODO
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
    BaseButton()
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