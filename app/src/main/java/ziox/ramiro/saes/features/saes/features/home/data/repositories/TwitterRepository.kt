package ziox.ramiro.saes.features.saes.features.home.data.repositories

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import ziox.ramiro.saes.features.saes.features.home.data.models.Tweet
import ziox.ramiro.saes.features.saes.features.home.data.models.TwitterResponse

interface TwitterRepository {
    suspend fun getTimelineTweets() : List<Tweet>
}


class TwitterRetrofitRepository : TwitterRepository {
    private interface TwitterAPI{
        companion object{
            fun build(): TwitterAPI = Retrofit.Builder()
                .baseUrl("https://api.twitter.com/")
                .client(OkHttpClient.Builder()
                    .addInterceptor {
                        it.proceed(it.request().newBuilder()
                            .addHeader("Authorization", Firebase.remoteConfig.getString("twitter_token"))
                            .build())
                    }
                    .build())
                .addConverterFactory(MoshiConverterFactory.create())
                .build().create(TwitterAPI::class.java)
        }

        @GET("/2/users/{userId}/tweets?max_results=5&user.fields=username,name,profile_image_url,verified&tweet.fields=created_at,attachments&expansions=author_id,attachments.media_keys&media.fields=preview_image_url,url&exclude=retweets,replies")
        suspend fun getTimeline(@Path("userId") userId: String): TwitterResponse
    }

    override suspend fun getTimelineTweets(): List<Tweet> {
        val api = TwitterAPI.build()
        val response = ArrayList<Tweet>()

        val secretariaIpn = api.getTimeline("3030986693")
        val ipnMX = api.getTimeline("302901861")

        response.addAll(Tweet.fromTwitterResponse(secretariaIpn))
        response.addAll(Tweet.fromTwitterResponse(ipnMX))

        return response.sortedByDescending {
            it.timestamp
        }
    }

}