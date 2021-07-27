package ziox.ramiro.saes.features.saes.features.home.data.repositories

import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.models.Tweet
import com.twitter.sdk.android.tweetui.TimelineResult
import com.twitter.sdk.android.tweetui.UserTimeline
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import java.util.*
import java.util.concurrent.TimeoutException
import kotlin.collections.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
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


        tweetList.addAll(withTimeout(5000){
            suspendCancellableCoroutine<List<Tweet>> {
                ipnMx.next(null, object : Callback<TimelineResult<Tweet>>(){
                    override fun success(result: com.twitter.sdk.android.core.Result<TimelineResult<Tweet>>) {
                        it.resume(result.data.items)
                    }
                    override fun failure(exception: TwitterException?) {
                        it.resumeWithException(exception ?: TimeoutException())

                    }
                })

            }
        })

        tweetList.addAll(withTimeout(5000){
            suspendCancellableCoroutine<List<Tweet>> {
                secretariaIpn.next(null, object : Callback<TimelineResult<Tweet>>(){
                    override fun success(result: com.twitter.sdk.android.core.Result<TimelineResult<Tweet>>) {
                        it.resume(result.data.items)
                    }
                    override fun failure(exception: TwitterException?) {
                        it.resumeWithException(exception ?: TimeoutException())

                    }
                })

            }
        })

        return tweetList.sortedByDescending {
            Date.parse(it.createdAt)
        }
    }
}