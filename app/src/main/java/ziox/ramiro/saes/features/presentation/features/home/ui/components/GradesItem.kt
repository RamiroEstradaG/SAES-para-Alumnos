package ziox.ramiro.saes.features.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import kotlin.random.Random

@Preview
@Composable
fun GradesItem(
    modifier: Modifier = Modifier,
    className: String = "Calificaciones",
    finalGrade: Int = Random.nextInt(10),
    onClick: () -> Unit = {}
) = Card(
    modifier = modifier
        .clip(MaterialTheme.shapes.medium)
        .size(74.dp, 90.dp)
        .clickable(
            interactionSource = MutableInteractionSource(),
            onClick = onClick,
            indication = rememberRipple()
        )
) {
    Column(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = className,
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Text(
            text = finalGrade.toString(),
            style = MaterialTheme.typography.h4,
            textAlign = TextAlign.Center,
            color = if(finalGrade < 6){
                getCurrentTheme().danger
            }else{
                getCurrentTheme().info
            }
        )
    }
}