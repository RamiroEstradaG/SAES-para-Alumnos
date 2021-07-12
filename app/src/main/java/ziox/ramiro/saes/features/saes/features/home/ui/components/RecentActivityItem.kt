package ziox.ramiro.saes.features.saes.features.home.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.features.saes.data.models.HistoryItem


@Composable
fun RecentActivityItem(
    modifier: Modifier = Modifier,
    historyItem: HistoryItem?,
    onClick: () -> Unit = {}
) = if (historyItem != null){
    Card(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .height(132.dp)
            .clickable(
                interactionSource = MutableInteractionSource(),
                onClick = onClick,
                indication = rememberRipple()
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier
                    .size(38.dp)
                    .padding(bottom = 8.dp),
                imageVector = historyItem.section.icon,
                contentDescription = "Recent activity",
                tint = MaterialTheme.colors.primary
            )
            Text(
                text = historyItem.section.sectionName,
                style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.primary
            )
        }
    }
}else{
    Box(
        modifier = Modifier.height(132.dp)
    ){}
}