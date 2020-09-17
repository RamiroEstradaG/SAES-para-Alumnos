package ziox.ramiro.saes.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import ziox.ramiro.saes.databases.User
import ziox.ramiro.saes.fragments.SelectSchoolNivelMedioSuperiorFragment


/**
 * Creado por Ramiro el 10/12/2018 a las 7:31 PM para SAESv2.
 */

fun downloadFile(context: Context?, name : String){
    context?.startActivity(
        Intent(Intent.ACTION_VIEW)
            .setData(
                Uri.parse(
                    getUrl(context)+"PDF/Alumnos/Reinscripciones/${getBoleta(context)}-$name.pdf"
                )
            )
    )
}


fun getBasicUser(context: Context?) = User(
    getNameEscuela(context),
    getCarrera(context).toProperCase(),
    SelectSchoolNivelMedioSuperiorFragment.medioSuperiorMap.containsKey(getCarrera(context))
)




