package ziox.ramiro.saes.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.MainActivity
import ziox.ramiro.saes.databinding.FragmentSelectSchoolBinding
import ziox.ramiro.saes.databinding.ViewSchoolSelectorItemBinding
import ziox.ramiro.saes.utils.removePreference
import ziox.ramiro.saes.utils.addBottomInsetPadding
import ziox.ramiro.saes.utils.setPreference
import java.util.*

/**
 * Creado por Ramiro el 10/12/2018 a las 6:23 PM para SAESv2.
 */
class SelectUniversityFragment : Fragment() {
    private val crashlytics = FirebaseCrashlytics.getInstance()
    companion object {
        val superiorMap = sortedMapOf(
            "ESIME Azcapotzalco" to "https://www.saes.esimeazc.ipn.mx/",
            "ESIME Culhuacan" to "https://www.saes.esimecu.ipn.mx/",
            "ESIME Ticomán" to "https://www.saes.esimetic.ipn.mx/",
            "ESIME Zacatenco" to "https://www.saes.esimez.ipn.mx/",
            "ESIA Tecamachalco" to "https://www.saes.esiatec.ipn.mx/",
            "ESIA Ticomán" to "https://www.saes.esiatic.ipn.mx/",
            "ESIA Zacatenco" to "https://www.saes.esiaz.ipn.mx/",
            "CICS Milpa Alta" to "https://www.saes.cicsma.ipn.mx/",
            "CICS Santo Tomas" to "https://www.saes.cicsst.ipn.mx/",
            "ESCA Santo Tomas" to "https://www.saes.escasto.ipn.mx/",
            "ESCA Tepepan" to "https://www.saes.escatep.ipn.mx/",
            "ENCB" to "https://www.saes.encb.ipn.mx/",
            "ENMH" to "https://www.saes.enmh.ipn.mx/",
            "ESEO" to "https://www.saes.eseo.ipn.mx/",
            "ESM" to "https://www.saes.esm.ipn.mx/",
            "ESE" to "https://www.saes.ese.ipn.mx/",
            "EST" to "https://www.saes.est.ipn.mx/",
            "UPIBI" to "https://www.saes.upibi.ipn.mx/",
            "UPIICSA" to "https://www.saes.upiicsa.ipn.mx/",
            "UPIITA" to "https://www.saes.upiita.ipn.mx/",
            "ESCOM" to "https://www.saes.escom.ipn.mx/",
            "ESFM" to "https://www.saes.esfm.ipn.mx/",
            "ESIQIE" to "https://www.saes.esiqie.ipn.mx/",
            "ESIT" to "https://www.saes.esit.ipn.mx/",
            "UPIIG" to "https://www.saes.upiig.ipn.mx/",
            "UPIIH" to "https://www.saes.upiih.ipn.mx/",
            "UPIIZ" to "https://www.saes.upiiz.ipn.mx/",
            "ENBA" to "https://www.saes.enba.ipn.mx/",
            "UPIIC" to "https://www.saes.upiic.ipn.mx/",
            "UPIIP" to "https://www.saes.upiip.ipn.mx/",
            "UPIEM" to "https://www.saes.upiem.ipn.mx/"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = FragmentSelectSchoolBinding.inflate(inflater, container, false)
        rootView.selectSchoolRecycler.addBottomInsetPadding()
        rootView.selectSchoolRecycler.layoutManager = LinearLayoutManager(context)
        rootView.selectSchoolRecycler.adapter = Adapter()
        rootView.selectSchoolRecycler.hasFixedSize()
        return rootView.root
    }

    inner class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(ViewSchoolSelectorItemBinding.inflate(layoutInflater, parent, false))
        }

        override fun getItemCount(): Int {
            return superiorMap.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val key = superiorMap.keys.elementAt(position)
            val data = superiorMap[key]

            val schoolNameSplit = key.split(" ", limit = 2)

            if(schoolNameSplit.size == 2){
                holder.schoolNameTextView.text = schoolNameSplit[0]
                holder.schoolPlaceTextView.text = schoolNameSplit[1]
            }else{
                holder.schoolNameTextView.text = key
                holder.schoolPlaceTextView.text = ""
            }

            try {
                val res = resources.getIdentifier(
                    holder.schoolNameTextView.text.toString().toLowerCase(Locale.ROOT),
                    "drawable",
                    activity?.packageName
                )
                holder.schoolLogoImageView.setImageResource(
                    if (res > 0) {
                        res
                    } else {
                        R.drawable.ic_logopoli
                    }
                )
            } catch (e: Exception) {
                Log.e(this.javaClass.canonicalName, e.toString())
            }


            holder.parent.setOnClickListener {
                crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                removePreference(activity, "new_url_escuela")
                removePreference(activity, "name_escuela")
                setPreference(activity, "new_url_escuela", data as Any)
                setPreference(activity, "name_escuela", holder.schoolNameTextView.text as Any)
                if (activity is MainActivity) {
                    (activity as MainActivity?)?.onSchoolSelected()
                }
            }
        }

        inner class ViewHolder(selectorItemBinding: ViewSchoolSelectorItemBinding) : RecyclerView.ViewHolder(selectorItemBinding.root) {
            val schoolNameTextView: TextView = selectorItemBinding.schoolNameTextView
            val schoolPlaceTextView: TextView = selectorItemBinding.schoolPlaceTextView
            val schoolLogoImageView: ImageView = selectorItemBinding.schoolLogoImageView
            val parent: LinearLayout = selectorItemBinding.parent
        }
    }
}