package ziox.ramiro.saes.features.presentation.features.home.data.repositories

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ziox.ramiro.saes.data.data_provider.retrofitProvider
import ziox.ramiro.saes.features.presentation.features.home.data.models.MappedTweet
import ziox.ramiro.saes.features.presentation.features.home.data.models.TimelineResponse

interface TwitterRepository {
    suspend fun getTimelineTweets() : List<MappedTweet>
}


class TwitterRetrofitRepository : TwitterRepository {
    private interface RetrofitAPI {
        companion object {
            fun create(): RetrofitAPI =
                retrofitProvider("https://api.twitter.com/", "Bearer AAAAAAAAAAAAAAAAAAAAAMHqLQEAAAAA1laskL%2BW5HNYSZu8jCdZuiswwZo%3DquFrNWN7RwS8Ixroz9JRDOyAhixm7lQ6MlU0e4PItaYHHHWbke")
                .create(RetrofitAPI::class.java)
        }

        @GET("/2/users/{id}/tweets?max_results=5&user.fields=username,name,profile_image_url&tweet.fields=created_at&expansions=author_id")
        suspend fun getTimelineTweets(
            @Path("id") userId: String
        ) : TimelineResponse
    }

    override suspend fun getTimelineTweets(): List<MappedTweet> {
        val tweetList = ArrayList<MappedTweet>()
        val api = RetrofitAPI.create()

        val ipnMx = api.getTimelineTweets("302901861")
        val secretariaIpn = api.getTimelineTweets("3030986693")

        tweetList.addAll(ipnMx.data.map {
            MappedTweet(
                it.id,
                it.date,
                ipnMx.includes.user.first { user -> user.userId == it.userId },
                it.content
            )
        })
        tweetList.addAll(secretariaIpn.data.map {
            MappedTweet(
                it.id,
                it.date,
                secretariaIpn.includes.user.first { user -> user.userId == it.userId },
                it.content
            )
        })

        return tweetList.sortedByDescending {
            it.date
        }
    }
}