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
import kotlinx.android.synthetic.main.fragment_select_school.view.*
import kotlinx.android.synthetic.main.item_escuelas.view.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.MainActivity
import ziox.ramiro.saes.utils.removePreference
import ziox.ramiro.saes.utils.addBottomInsetPadding
import ziox.ramiro.saes.utils.setPreference

/**
 * Creado por Ramiro el 10/12/2018 a las 6:23 PM para SAESv2.
 */
class SelectSchoolNivelSuperiorFragment : Fragment() {
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
            "ENBA" to "https://148.204.250.37/",
            "UPIBI" to "https://www.saes.upibi.ipn.mx/",
            "UPIICSA" to "https://www.saes.upiicsa.ipn.mx/",
            "UPIITA" to "https://www.saes.upiita.ipn.mx/",
            "ESCOM" to "https://www.saes.escom.ipn.mx/",
            "ESFM" to "https://www.saes.esfm.ipn.mx/",
            "ESIQIE" to "https://www.saes.esiqie.ipn.mx/",
            "ESIT" to "https://www.saes.esit.ipn.mx/",
            "UPIIG" to "https://www.saes.upiig.ipn.mx/",
            "UPIIH" to "https://148.204.250.36/",
            "UPIIZ" to "https://www.saes.upiiz.ipn.mx/"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_select_school, container, false)
        rootView.selectSchoolRecycler.addBottomInsetPadding()
        rootView.selectSchoolRecycler.layoutManager = LinearLayoutManager(context)
        rootView.selectSchoolRecycler.adapter = Adapter()
        rootView.selectSchoolRecycler.hasFixedSize()
        return rootView
    }

    inner class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_escuelas,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int {
            return superiorMap.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val key = superiorMap.keys.elementAt(holder.adapterPosition)
            val data = superiorMap[key]

            try {
                holder.textViewName.text = key.split(" ", limit = 2)[0]
                holder.textViewLugar.text = key.split(" ", limit = 2)[1]
            } catch (e: Exception) {
                Log.e("AppException", e.toString())
                holder.textViewName.text = key
                holder.textViewLugar.text = ""
            }

            try {
                val res = resources.getIdentifier(
                    holder.textViewName.text.toString().toLowerCase(),
                    "drawable",
                    activity?.packageName
                )
                holder.iconSchool.setImageResource(
                    if (res > 0) {
                        res
                    } else {
                        R.drawable.ic_logopoli
                    }
                )
            } catch (e: Exception) {
                Log.e("AppException", e.toString())
            }


            holder.parent.setOnClickListener {
                crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                removePreference(activity, "new_url_escuela")
                removePreference(activity, "name_escuela")
                setPreference(activity, "new_url_escuela", data as Any)
                setPreference(activity, "name_escuela", holder.textViewName.text as Any)
                if (activity is MainActivity) {
                    (activity as MainActivity?)?.onEscuelaSelected()
                }
            }
        }

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val textViewName: TextView = v.textViewName
            val textViewLugar: TextView = v.textViewLugar
            val iconSchool: ImageView = v.imageViewSchool
            val parent: LinearLayout = v.parentItemEscuelas
        }
    }
}