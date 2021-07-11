package ziox.ramiro.saes.features.saes.features.home.ui.components

import android.view.View
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.twitter.sdk.android.core.models.Tweet
import com.twitter.sdk.android.tweetui.R
import com.twitter.sdk.android.tweetui.TweetView

@ExperimentalMaterialApi
@Composable
fun TwitterItem(
    modifier: Modifier = Modifier,
    tweet: Tweet
) {
    val isDarkTheme = isSystemInDarkTheme()

    Card(
        modifier = modifier,
    ) {
        AndroidView(
            factory = {
                try{
                    TweetView(it, tweet, if(isDarkTheme){
                        R.style.tw__TweetDarkStyle
                    }else{
                        R.style.tw__TweetLightStyle
                    })
                }catch (e: Exception){
                    View(it)
                }
            }
        )
    }
}