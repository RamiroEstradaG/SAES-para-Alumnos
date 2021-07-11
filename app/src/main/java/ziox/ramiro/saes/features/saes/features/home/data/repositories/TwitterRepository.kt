package ziox.ramiro.saes.features.saes.features.home.data.repositories

import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.models.Tweet
import com.twitter.sdk.android.tweetui.TimelineResult
import com.twitter.sdk.android.tweetui.UserTimeline
import java.util.*
import java.util.concurrent.TimeoutException
import kotlin.collections.ArrayList
import kotlin.coroutines.suspendCoroutine

interface TwitterRepository {
    suspend fun getTimelineTweets() : List<Tweet>
}


class TwitterAPIRepository : TwitterRepository {
    override suspend fun getTimelineTweets(): List<Tweet> {
        val tweetList = ArrayList<Tweet>()

        val secretariaIpn = UserTimeline.Builder()
            .screenName("SecretariaIPN")
            .includeRetweets(false)
            .includeReplies(false)
            .maxItemsPerRequest(5).build()

        val ipnMx = UserTimeline.Builder()
            .screenName("IPN_MX")
            .includeRetweets(false)
            .includeReplies(false)
            .maxItemsPerRequest(5).build()


        tweetList.addAll(suspendCoroutine<List<Tweet>> {
            ipnMx.next(null, object : Callback<TimelineResult<Tweet>>(){
                override fun success(result: com.twitter.sdk.android.core.Result<TimelineResult<Tweet>>) {
                    it.resumeWith(Result.success(result.data.items))
                }
                override fun failure(exception: TwitterException?) {
                    it.resumeWith(Result.failure(exception ?: TimeoutException()))
                }
            })
        })

        tweetList.addAll(suspendCoroutine<List<Tweet>> {
            secretariaIpn.next(null, object : Callback<TimelineResult<Tweet>>(){
                override fun success(result: com.twitter.sdk.android.core.Result<TimelineResult<Tweet>>) {
                    it.resumeWith(Result.success(result.data.items))
                }
                override fun failure(exception: TwitterException?) {
                    it.resumeWith(Result.failure(exception ?: TimeoutException()))
                }
            })
        })

        return tweetList.sortedByDescending {
            Date.parse(it.createdAt)
        }
    }
}