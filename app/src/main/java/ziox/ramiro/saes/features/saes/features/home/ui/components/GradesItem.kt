package ziox.ramiro.saes.features.saes.features.home.ui.components

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
import ziox.ramiro.saes.features.saes.features.ets.data.models.ETSScore
import ziox.ramiro.saes.features.saes.features.grades.data.models.ClassGrades
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.utils.getInitials

@Composable
fun SmallGradeItem(
    modifier: Modifier = Modifier,
    classGrades: ClassGrades,
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
            text = classGrades.className.getInitials(),
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Text(
            text = classGrades.finalScore?.toString() ?: "-",
            style = MaterialTheme.typography.h4,
            textAlign = TextAlign.Center,
            color = gradeColor(classGrades.finalScore)
        )
    }
}

@Composable
fun SmallGradeItem(
    modifier: Modifier = Modifier,
    etsScore: ETSScore,
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
            text = etsScore.className.getInitials(),
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Text(
            text = etsScore.grade.toString() ?: "-",
            style = MaterialTheme.typography.h4,
            textAlign = TextAlign.Center,
            color = gradeColor(etsScore.grade)
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
            Grade(title = "Final", grade = classGrades.finalScore)
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
        color = gradeColor(grade)
    )
}

@Composable
fun gradeColor(grade: Int?) = when{
    grade == null -> getCurrentTheme().primaryText
    grade < 6 -> getCurrentTheme().danger
    else -> getCurrentTheme().info
}