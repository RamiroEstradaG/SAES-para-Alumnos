package ziox.ramiro.saes.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme

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
        shape = RoundedCornerShape(100),

        ) {
        if (isLoadingState.value != true){
            Text(
                text = text.uppercase(),
                modifier = Modifier.padding(if (isHighEmphasis) 6.dp else 0.dp)
            )
        }else{
            Box {
                CircularProgressIndicator(
                    color = Color.White,
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


@Preview
@Composable
fun BaseButtonPreview() = SAESParaAlumnosTheme {
    BaseButton()
}

@Preview
@Composable
fun AsyncButtonPreview() = SAESParaAlumnosTheme {
    BaseButton()
}