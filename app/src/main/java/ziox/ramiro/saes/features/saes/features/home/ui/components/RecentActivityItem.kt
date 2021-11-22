package ziox.ramiro.saes.features.saes.features.home.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.features.saes.data.models.HistoryItem
import ziox.ramiro.saes.features.saes.view_models.MenuSection
import java.util.Date


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
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
                indication = ripple()
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                modifier = Modifier
                    .size(38.dp)
                    .padding(bottom = 8.dp),
                imageVector = historyItem.section.icon,
                contentDescription = "Recent activity",
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = historyItem.section.sectionName,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2
            )
        }
    }
}else{
    Box(
        modifier = Modifier.height(132.dp)
    ){}
}

@Composable
@Preview
fun RecentActivityItemPreview() = RecentActivityItem(
    historyItem = HistoryItem(
        id = 0,
        date = Date(),
        section = MenuSection.KARDEX
    )
) {

}