package ziox.ramiro.saes.features.saes.features.home.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TwitterResponse(
    val data: List<ResponseTweet>,
    val includes: ResponseIncludes
)

@JsonClass(generateAdapter = true)
data class ResponseTweet(
    val id: String,
    val author_id: String,
    val created_at: String,
    val text: String,
    val attachments: ResponseAttachments?
)

@JsonClass(generateAdapter = true)
data class ResponseAttachments(
    val media_keys: List<String>
)

@JsonClass(generateAdapter = true)
data class ResponseIncludes(
    val users: List<User>,
    val media: List<ResponseMedia>
)

@JsonClass(generateAdapter = true)
data class User(
    val id: String,
    val name: String,
    val username: String,
    val profile_image_url: String,
    val verified: Boolean
)

@JsonClass(generateAdapter = true)
data class ResponseMedia(
    val media_key: String,
    val type: String,
    val url: String?,
    val preview_image_url: String?
)