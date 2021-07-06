package ziox.ramiro.saes.features.presentation.features.home.ui.components

import android.text.util.Linkify
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.coil.rememberCoilPainter
import ziox.ramiro.saes.features.presentation.features.home.data.models.MappedTweet
import ziox.ramiro.saes.features.presentation.features.home.data.models.TwitterUser
import java.util.*

@ExperimentalMaterialApi
@Composable
fun TwitterItem(
    modifier: Modifier = Modifier,
    tweet: MappedTweet
) = Card(
    modifier,
    elevation = 0.dp
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clip(CircleShape)
                    .size(34.dp),
                painter = rememberCoilPainter(
                    request = tweet.user.profilePictureUrl,
                    fadeIn = true
                ),
                contentDescription = "Profile picture",
            )
            Column {
                Text(
                    text = tweet.user.username,
                    style = MaterialTheme.typography.subtitle2
                )
                Text(
                    text = "@${tweet.user.userTag}",
                    style = MaterialTheme.typography.body2
                )
            }
        }
        AndroidView(
            factory = {
                val text = TextView(it)

                text.text = tweet.content

                Linkify.addLinks(text, Linkify.ALL)
                text.linksClickable = true

                text
            }
        )
    }
}

@ExperimentalMaterialApi
@Preview
@Composable
private fun TwitterPreview(){
    TwitterItem(tweet = MappedTweet(
        "",
        Date(),
        TwitterUser(
            "",
            "Username",
            "name",
            ""
        ),
        "Hola"
    ))
}