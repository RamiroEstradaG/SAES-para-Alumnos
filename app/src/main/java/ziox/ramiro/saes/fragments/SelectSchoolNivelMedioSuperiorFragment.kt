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
 * Creado por Ramiro el 10/12/2018 a las 6:28 PM para SAESv2.
 */
class SelectSchoolNivelMedioSuperiorFragment : Fragment() {
    private val crashlytics = FirebaseCrashlytics.getInstance()
    companion object {
        val medioSuperiorMap = mapOf(
            "CECyT 1" to "https://www.saes.cecyt1.ipn.mx/",
            "CECyT 2" to "https://www.saes.cecyt2.ipn.mx/",
            "CECyT 3" to "https://www.saes.cecyt3.ipn.mx/",
            "CECyT 4" to "https://www.saes.cecyt4.ipn.mx/",
            "CECyT 5" to "https://www.saes.cecyt5.ipn.mx/",
            "CECyT 6" to "https://www.saes.cecyt6.ipn.mx/",
            "CECyT 7" to "https://www.saes.cecyt7.ipn.mx/",
            "CECyT 8" to "https://www.saes.cecyt8.ipn.mx/",
            "CECyT 9" to "https://www.saes.cecyt9.ipn.mx/",
            "CECyT 10" to "https://www.saes.cecyt10.ipn.mx/",
            "CECyT 11" to "https://www.saes.cecyt11.ipn.mx/",
            "CECyT 12" to "https://www.saes.cecyt12.ipn.mx/",
            "CECyT 13" to "https://www.saes.cecyt13.ipn.mx/",
            "CECyT 14" to "https://www.saes.cecyt14.ipn.mx/",
            "CECyT 15" to "https://www.saes.cecyt15.ipn.mx/",
            "CECyT 16" to "https://www.saes.cecyt16.ipn.mx/",
            "CECyT 17" to "https://148.204.250.26/",
            "CECyT 18" to "https://148.204.250.24/",
            "CET 1" to "https://www.saes.cet1.ipn.mx/"
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
            return medioSuperiorMap.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val key = medioSuperiorMap.keys.elementAt(holder.adapterPosition)
            val data = medioSuperiorMap[key]

            holder.textViewName.text = key
            holder.textViewLugar.text = ""


            try {
                val res = resources.getIdentifier(
                    holder.textViewName.text.toString().toLowerCase().replace(
                        " ",
                        ""
                    ), "drawable", activity?.packageName
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