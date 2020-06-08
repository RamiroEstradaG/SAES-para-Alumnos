package ziox.ramiro.saes.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomappbar.BottomAppBar
import kotlinx.android.synthetic.main.fragment_calendario_trabajo.view.*
import kotlinx.android.synthetic.main.view_calendario_trabajo_item.view.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.dialogs.AddCalendarioTrabajoDialog
import ziox.ramiro.saes.utils.*


class CalendarioTrabajoFragment : Fragment() {
    private lateinit var rootView : View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_calendario_trabajo, container, false)
        rootView.calendarioTrabajoLayout.addBottomInsetPadding()

        val progressBar = (activity as SAESActivity).getProgressBar()
        if(activity is SAESActivity){
            (activity as SAESActivity).showFab(R.drawable.ic_add_black_24dp, View.OnClickListener {
                val dialog = AddCalendarioTrabajoDialog()
                dialog.setOnSuccessListener {
                    (activity as SAESActivity).postNavigationItemSelected(R.id.nav_calendario_trabajo, false)
                }
                dialog.show(childFragmentManager, "add_calendario_trabajo")
            }, BottomAppBar.FAB_ALIGNMENT_MODE_END)
        }

        initUser(activity, getBoleta(activity), getBasicUser(activity)){
            getUserData(activity){
                initCalendarios(it)
            }
        }



        rootView.switchNotifications.isChecked = getPreference(activity, "calendario_trabajo_notification", false)
        rootView.switchNotifications.setOnCheckedChangeListener { _, b ->
            setPreference(activity, "calendario_trabajo_notification", b)
        }

        activity?.runOnUiThread {
            progressBar?.visibility = View.VISIBLE
        }

        return rootView
    }

    private fun initCalendarios(user: User){
        if(user.calendariosId.isEmpty()) {
            activity?.runOnUiThread {
                (activity as SAESActivity).getProgressBar()?.visibility = View.GONE
                (activity as SAESActivity).showEmptyText("No tienes calendarios")
            }

            return
        }
        getCalendarios(user.calendariosId){ snap ->
            activity?.runOnUiThread {
                (activity as SAESActivity).getProgressBar()?.visibility = View.GONE
            }

            if(snap.isNotEmpty() && activity != null){
                for (doc in snap){
                    val lay = LayoutInflater.from(activity).inflate(R.layout.view_calendario_trabajo_item, null, false)

                    lay.calendarioTrabajoNombre.text = doc.name
                    lay.calendarioTrabajoTipo.text = if(!doc.private){
                        "Grupal"
                    }else{
                        "Personal"
                    }

                    if(!doc.private){
                        lay.calendarioTrabajoCodigo.text = "Código: ${doc.codigo}"
                    }else{
                        lay.calendarioTrabajoCodigo.visibility = View.GONE
                    }

                    lay.calendarioTrabajoButton.setOnClickListener {
                        if(activity is SAESActivity){
                            (activity as SAESActivity).fragmentReplace(CalendarioTrabajoViewerFragment(doc.codigo, doc.admin), -1)
                        }
                    }

                    lay.removeCalendarioTrabajo.setOnClickListener {
                        val alertDialog = AlertDialog.Builder(activity, R.style.DialogAlert)

                        alertDialog.setTitle("Borrar ${doc.name}")
                        alertDialog.setMessage("¿Desea borrar este calendario de trabajo?")
                        alertDialog.setPositiveButton("Borrar"){ _, _ ->
                            removeCalendario(activity, doc.codigo).addOnSuccessListener {
                                if(activity is SAESActivity){
                                    (activity as SAESActivity).postNavigationItemSelected(R.id.nav_calendario_trabajo, false)
                                }
                            }
                        }
                        alertDialog.setNegativeButton("Cancelar", null)

                        alertDialog.show()
                    }

                    rootView.calendarioTrabajoLayout.addView(lay)
                }
            }
        }
    }
}