package ziox.ramiro.saes.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.R
import ziox.ramiro.saes.data.models.School
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.ui.theme.getCurrentTheme

@Composable
fun SchoolButton(
    modifier: Modifier = Modifier,
    school: School? = null,
    isSmall: Boolean = false,
    onClick: () -> Unit = {}
) = Row(
    modifier = modifier
        .clip(MaterialTheme.shapes.medium)
        .clickable(
            indication = rememberRipple(),
            onClick = onClick,
            interactionSource = MutableInteractionSource()
        )
        .background(MaterialTheme.colors.surface)
        .fillMaxWidth()
        .padding(
            horizontal = 12.dp,
            vertical = if(isSmall) 8.dp else 10.dp
        ),
    verticalAlignment = Alignment.CenterVertically
) {
    Image(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .size(if(isSmall) 38.dp else 58.dp)
            .background(MaterialTheme.colors.background)
            .padding(if(isSmall) 2.dp else 4.dp),
        painter = painterResource(id = school?.logoId ?: R.drawable.ic_logopoli),
        contentDescription = school?.schoolName ?: ""
    )
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = if(isSmall) 8.dp else 16.dp)
    ) {
        Text(
            text = school?.schoolName ?: "",
            style = MaterialTheme.typography.h5
        )
        if(school?.schoolLocation != null){
            Text(
                text = school.schoolLocation,
                style = MaterialTheme.typography.subtitle2
            )
        }
    }
    Icon(
        modifier = Modifier
            .clip(CircleShape)
            .size(24.dp)
            .background(MaterialTheme.colors.background)
            .padding(1.dp),
        imageVector = Icons.Rounded.KeyboardArrowRight,
        contentDescription = "Right",
        tint = getCurrentTheme().primaryText
    )
}

@Preview
@Composable
fun PreviewSchoolButton() = SAESParaAlumnosTheme {
    SchoolButton()
}