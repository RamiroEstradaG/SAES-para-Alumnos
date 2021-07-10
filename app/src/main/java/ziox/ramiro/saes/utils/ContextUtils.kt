package ziox.ramiro.saes.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.launchUrl(url: String){
    startActivity(Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(url)
    })
}