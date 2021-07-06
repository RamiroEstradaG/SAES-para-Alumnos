package ziox.ramiro.saes.features.saes.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.features.saes.presentation.features.grades.data.models.ClassGrades
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.utils.getInitials
import kotlin.random.Random

@Preview
@Composable
fun SmallGradeItem(
    modifier: Modifier = Modifier,
    className: String = "Nombre de Una Materia Extremadamente Larga XI",
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
        modifier = Modifier.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = className.getInitials(),
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
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


@Composable
fun GradeItem(
    modifier: Modifier = Modifier,
    classGrades: ClassGrades
) = Card(modifier) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = classGrades.className,
            style = MaterialTheme.typography.h5,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Grade(title = "1ro", grade = classGrades.p1)
            Grade(title = "2do", grade = classGrades.p2)
            Grade(title = "3ro", grade = classGrades.p3)
            Grade(title = "Extra", grade = classGrades.extra)
            Grade(title = "Final", grade = classGrades.final)
        }
    }
}

@Composable
private fun Grade(
    title: String,
    grade: Int?
) = Column(
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(
        text = title,
        style = MaterialTheme.typography.subtitle1
    )
    Text(
        text = grade?.toString() ?: "-",
        style = MaterialTheme.typography.h4,
        color = when{
            grade == null -> getCurrentTheme().primaryText
            grade < 6 -> getCurrentTheme().danger
            else -> getCurrentTheme().info
        }
    )
}