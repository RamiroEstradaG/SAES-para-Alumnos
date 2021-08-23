package ziox.ramiro.saes.features.saes.features.home.data.models

import java.text.SimpleDateFormat
import java.util.*


data class Tweet(
    val user: User,
    val text: String,
    val image: String?,
    val timestamp: Long
) {
    companion object{
        fun fromTwitterResponse(twitterResponse: TwitterResponse) = twitterResponse.data.map { tweet ->
            val singleImage = twitterResponse.includes.media.find { it.media_key == tweet.attachments?.media_keys?.singleOrNull() }
            val image = if(singleImage?.type == "photo"){
                singleImage.url
            }else{
                singleImage?.preview_image_url
            }
            Tweet(
                twitterResponse.includes.users.find { it.id == tweet.author_id }!!,
                tweet.text,
                image,
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(tweet.created_at)?.time!!
            )
        }
    }

    fun timeAgo(): String {
        val currentTime = Date().time
        val timeDiff = currentTime - timestamp
        return when {
            timeDiff >= (1000 * 60 * 60 * 24) -> "${timeDiff / (1000 * 60 * 60 * 24)}d"
            timeDiff >= (1000 * 60 * 60) -> "${timeDiff / (1000 * 60 * 60)}h"
            timeDiff >= (1000 * 60) -> "${timeDiff / (1000 * 60)}m"
            timeDiff >= 1000 -> "${timeDiff / 1000}s"
            else -> "0s"
        }
    }
}

