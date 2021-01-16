package ziox.ramiro.saes.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import ziox.ramiro.saes.databases.User
import ziox.ramiro.saes.fragments.SelectHighSchoolFragment
import java.io.ByteArrayOutputStream


/**
 * Creado por Ramiro el 10/12/2018 a las 7:31 PM para SAESv2.
 */

fun downloadFile(context: Context?, name: String){
    context?.startActivity(
        Intent(Intent.ACTION_VIEW)
            .setData(
                Uri.parse(
                    getUrl(context) + "PDF/Alumnos/Reinscripciones/${getBoleta(context)}-$name.pdf"
                )
            )
    )
}


fun getBasicUser(context: Context?) = User(
    getSchoolName(context),
    getCareerName(context).toProperCase(),
    SelectHighSchoolFragment.highSchoolMap.containsKey(getCareerName(context))
)

fun Drawable.toByteArray() : ByteArray{
    val bitmap = Bitmap.createBitmap(
        this.intrinsicWidth,
        this.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    this.setBounds(0, 0, canvas.width, canvas.height)
    this.draw(canvas)

    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
    return stream.toByteArray()
}


