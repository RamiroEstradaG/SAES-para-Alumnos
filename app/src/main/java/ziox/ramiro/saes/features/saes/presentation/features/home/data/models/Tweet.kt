package ziox.ramiro.saes.features.saes.presentation.features.home.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class TimelineResponse(
    @Json(name = "data") val data: List<Tweet>,
    @Json(name = "includes") val includes: TweetIncludes
)

@JsonClass(generateAdapter = true)
data class Tweet(
    @Json(name = "id")
    val id: String,
    @Json(name = "created_at")
    val date: Date,
    @Json(name = "author_id")
    val userId : String,
    @Json(name = "text")
    val content: String
)

@JsonClass(generateAdapter = true)
data class TweetIncludes(
    @Json(name = "users")
    val user : List<TwitterUser>,
)

@JsonClass(generateAdapter = true)
data class TwitterUser(
    @Json(name = "id")
    val userId: String,
    @Json(name = "name")
    val username: String,
    @Json(name = "username")
    val userTag: String,
    @Json(name = "profile_image_url")
    val profilePictureUrl: String
)


data class MappedTweet(
    val id: String,
    val date: Date,
    val user : TwitterUser,
    val content: String
)