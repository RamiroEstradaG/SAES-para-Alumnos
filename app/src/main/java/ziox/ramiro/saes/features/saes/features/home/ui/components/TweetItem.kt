package ziox.ramiro.saes.features.saes.features.home.ui.components

import android.text.util.Linkify
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.annotation.ExperimentalCoilApi
import coil.compose.LocalImageLoader
import coil.compose.rememberImagePainter
import ziox.ramiro.saes.R
import ziox.ramiro.saes.features.saes.features.home.data.models.Tweet
import ziox.ramiro.saes.ui.theme.getCurrentTheme

@OptIn(ExperimentalCoilApi::class)
@Composable
fun TweetItem(tweet: Tweet) {
    Card(
        modifier = Modifier.padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Row(modifier = Modifier.padding(all = 10.dp)) {
                UserAvatar(tweet)
                Spacer(modifier = Modifier.size(12.dp))
                Column {
                    NameAndUserName(tweet)
                    Spacer(modifier = Modifier.size(1.dp))
                    Tweet(tweet)
                }
            }
            if (tweet.image != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Image(
                    modifier = Modifier
                        .height(180.dp)
                        .fillMaxWidth(),
                    painter = rememberImagePainter(
                        data = tweet.image,
                        imageLoader = LocalImageLoader.current,
                    ),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Tweet and image"
                )
            }
        }
    }
}

@Composable
private fun Tweet(tweet: Tweet) {
    val secondaryText = getCurrentTheme().secondaryText

    AndroidView (
        factory = {
            TextView(it).apply {
                text = tweet.text
                Linkify.addLinks(this, Linkify.ALL)
                linksClickable = true
                setTextColor(secondaryText.toArgb())
            }
        }
    )
}

@Composable
private fun NameAndUserName(tweet: Tweet) {
    val density = LocalDensity.current
    val rowWidth = remember {
        mutableStateOf(0.dp)
    }
    val nameWidth = remember {
        mutableStateOf(0.dp)
    }
    val timeWidth = remember {
        mutableStateOf(0.dp)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                rowWidth.value = with(density) {
                    it.size.width.toDp()
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f, false)
                .wrapContentWidth(
                    unbounded = true,
                    align = Alignment.Start
                )
        ) {
            Row(
                modifier = Modifier.onGloballyPositioned {
                    nameWidth.value = with(density){
                        it.size.width.toDp()
                    }
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tweet.user.name,
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                )
                if (tweet.user.verified) {
                    Spacer(modifier = Modifier.size(2.dp))
                    Image(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(R.drawable.ic_twitter_verified_badge),
                        contentDescription = "Verified"
                    )
                }
                Spacer(modifier = Modifier.size(5.dp))
                Text(
                    text = "@${tweet.user.username}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(nameWidth.value - (rowWidth.value - timeWidth.value), with(density) {
                        16.sp.toDp()
                    })
                    .height(20.dp)
                    .width(50.dp)
                    .background(MaterialTheme.colorScheme.surface)
            )
        }
        Text(
            modifier = Modifier.onGloballyPositioned {
                timeWidth.value = with(density){
                    it.size.width.toDp()
                }
            },
            text = " · ${tweet.timeAgo()}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun UserAvatar(tweet: Tweet) {
    Image(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape),
        painter = rememberImagePainter(
            data = tweet.user.profile_image_url,
            imageLoader = LocalImageLoader.current,
        ),
        contentScale = ContentScale.Crop,
        contentDescription = "User avatar"
    )
}