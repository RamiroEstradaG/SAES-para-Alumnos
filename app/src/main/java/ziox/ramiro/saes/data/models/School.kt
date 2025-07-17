package ziox.ramiro.saes.data.models

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.DrawableRes
import ziox.ramiro.saes.R

data class School(
    val schoolName: String,
    val schoolLocation: String?,
    val url: String,
    @DrawableRes val logoId: Int = R.drawable.ic_logopoli
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString()!!,
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(schoolName)
        parcel.writeString(schoolLocation)
        parcel.writeString(url)
        parcel.writeInt(logoId)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<School> {
        override fun createFromParcel(parcel: Parcel): School {
            return School(parcel)
        }

        override fun newArray(size: Int): Array<School?> {
            return arrayOfNulls(size)
        }

        fun findSchoolByUrl(url: String) : School? {
            val university = universities.find {
                it.url == url
            }

            if(university != null) return university

            return highSchools.find {
                it.url == url
            }
        }
    }
}


val highSchools = listOf(
    School("CECyT 1", null, "https://www.saes.cecyt1.ipn.mx/", R.drawable.cecyt1),
    School("CECyT 2", null, "https://www.saes.cecyt2.ipn.mx/", R.drawable.cecyt2),
    School("CECyT 3", null, "https://www.saes.cecyt3.ipn.mx/", R.drawable.cecyt3),
    School("CECyT 4", null, "https://www.saes.cecyt4.ipn.mx/", R.drawable.cecyt4),
    School("CECyT 5", null, "https://www.saes.cecyt5.ipn.mx/", R.drawable.cecyt5),
    School("CECyT 6", null, "https://www.saes.cecyt6.ipn.mx/", R.drawable.cecyt6),
    School("CECyT 7", null, "https://www.saes.cecyt7.ipn.mx/", R.drawable.cecyt7),
    School("CECyT 8", null, "https://www.saes.cecyt8.ipn.mx/", R.drawable.cecyt8),
    School("CECyT 9", null, "https://www.saes.cecyt9.ipn.mx/", R.drawable.cecyt9),
    School("CECyT 10", null, "https://www.saes.cecyt10.ipn.mx/", R.drawable.cecyt10),
    School("CECyT 11", null, "https://www.saes.cecyt11.ipn.mx/", R.drawable.cecyt11),
    School("CECyT 12", null, "https://www.saes.cecyt12.ipn.mx/", R.drawable.cecyt12),
    School("CECyT 13", null, "https://www.saes.cecyt13.ipn.mx/", R.drawable.cecyt13),
    School("CECyT 14", null, "https://www.saes.cecyt14.ipn.mx/", R.drawable.cecyt14),
    School("CECyT 15", null, "https://www.saes.cecyt15.ipn.mx/", R.drawable.cecyt15),
    School("CECyT 16", null, "https://www.saes.cecyt16.ipn.mx/", R.drawable.cecyt16),
    School("CECyT 17", null, "https://www.saes.cecyt17.ipn.mx/", R.drawable.cecyt17),
    School("CECyT 18", null, "https://www.saes.cecyt18.ipn.mx/", R.drawable.cecyt18),
    School("CECyT 19", null, "https://www.saes.cecyt19.ipn.mx/", R.drawable.cecyt19),
    School("CET 1", null, "https://www.saes.cet1.ipn.mx/", R.drawable.cet1)
)

val universities = listOf(
    School("ESIME", "Azcapotzalco", "https://www.saes.esimeazc.ipn.mx/", R.drawable.esime),
    School("ESIME", "Culhuacan", "https://www.saes.esimecu.ipn.mx/", R.drawable.esime),
    School("ESIME", "Ticomán", "https://www.saes.esimetic.ipn.mx/", R.drawable.esime),
    School("ESIME", "Zacatenco", "https://www.saes.esimez.ipn.mx/", R.drawable.esime),
    School("ESIA", "Tecamachalco", "https://www.saes.esiatec.ipn.mx/", R.drawable.esia),
    School("ESIA", "Ticomán", "https://www.saes.esiatic.ipn.mx/", R.drawable.esia),
    School("ESIA", "Zacatenco", "https://www.saes.esiaz.ipn.mx/", R.drawable.esia),
    School("CICS", "Milpa Alta", "https://www.saes.cicsma.ipn.mx/", R.drawable.cics),
    School("CICS", "Santo Tomas", "https://www.saes.cicsst.ipn.mx/", R.drawable.cics),
    School("ESCA", "Santo Tomas", "https://www.saes.escasto.ipn.mx/", R.drawable.esca),
    School("ESCA", "Tepepan", "https://www.saes.escatep.ipn.mx/", R.drawable.esca),
    School("ENCB", null, "https://www.saes.encb.ipn.mx/", R.drawable.encb),
    School("ENMH", null, "https://www.saes.enmh.ipn.mx/", R.drawable.enmh),
    School("ESEO", null, "https://www.saes.eseo.ipn.mx/", R.drawable.eseo),
    School("ESM", null, "https://www.saes.esm.ipn.mx/", R.drawable.esm),
    School("ESE", null, "https://www.saes.ese.ipn.mx/", R.drawable.ese),
    School("EST", null, "https://www.saes.est.ipn.mx/", R.drawable.est),
    School("UPIBI", null, "https://www.saes.upibi.ipn.mx/", R.drawable.upibi),
    School("UPIICSA", null, "https://www.saes.upiicsa.ipn.mx/", R.drawable.upiicsa),
    School("UPIITA", null, "https://www.saes.upiita.ipn.mx/", R.drawable.upiita),
    School("ESCOM", null, "https://www.saes.escom.ipn.mx/", R.drawable.escom),
    School("ESFM", null, "https://www.saes.esfm.ipn.mx/", R.drawable.esfm),
    School("ESIQIE", null, "https://www.saes.esiqie.ipn.mx/", R.drawable.esiqie),
    School("ESIT", null, "https://www.saes.esit.ipn.mx/", R.drawable.esit),
    School("UPIIG", null, "https://www.saes.upiig.ipn.mx/", R.drawable.upiig),
    School("UPIIH", null, "https://www.saes.upiih.ipn.mx/"),
    School("UPIIZ", null, "https://www.saes.upiiz.ipn.mx/", R.drawable.upiiz),
    School("ENBA", null, "https://www.saes.enba.ipn.mx/", R.drawable.enba),
    School("UPIIC", null, "https://www.saes.upiic.ipn.mx/"),
    School("UPIIP", null, "https://www.saes.upiip.ipn.mx/", R.drawable.upiip),
    School("UPIEM", null, "https://www.saes.upiem.ipn.mx/"),
    School("UPIIT", null, "https://www.saes.upiit.ipn.mx/")
)