package ziox.ramiro.saes.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomappbar.BottomAppBar
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.databases.*
import ziox.ramiro.saes.databinding.FragmentUserCalendarBinding
import ziox.ramiro.saes.databinding.ViewUserCalendarItemBinding
import ziox.ramiro.saes.dialogs.AddCalendarDialogFragment
import ziox.ramiro.saes.utils.*


class UserCalendarFragment : Fragment() {
    private lateinit var rootView : FragmentUserCalendarBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = FragmentUserCalendarBinding.inflate(inflater, container, false)
        rootView.userCalendarLayout.addBottomInsetPadding()

        val progressBar = (activity as SAESActivity).getProgressBar()
        if(activity is SAESActivity){
            (activity as SAESActivity).showFab(R.drawable.ic_add_black_24dp, {
                val dialog = AddCalendarDialogFragment()
                dialog.setOnSuccessListener {
                    (activity as SAESActivity).postNavigationItemSelected(R.id.nav_personal_agenda, false)
                }
                dialog.show(childFragmentManager, "add_calendario_trabajo")
            }, BottomAppBar.FAB_ALIGNMENT_MODE_END)
        }

        initUser(activity, getBoleta(activity), getBasicUser(activity)){
            getUserData(activity){
                activity?.runOnUiThread {
                    initCalendars(it)
                }
            }
        }

        rootView.notificationEnablerSwitch.isChecked = getPreference(activity, "calendario_trabajo_notification", false)
        rootView.notificationEnablerSwitch.setOnCheckedChangeListener { _, b ->
            setPreference(activity, "calendario_trabajo_notification", b)
        }

        activity?.runOnUiThread {
            progressBar?.visibility = View.VISIBLE
        }

        return rootView.root
    }

    private fun addSchoolAgenda(){
        val calendarItem = ViewUserCalendarItemBinding.inflate(LayoutInflater.from(context))

        calendarItem.calendarTitleTextView.text = "Agenda escolar"
        calendarItem.calendarTypeTextView.text = "Académico"

        calendarItem.calendarCodeTextView.visibility = View.GONE

        calendarItem.calendarButton.setOnClickListener {
            if(activity is SAESActivity){
                (activity as SAESActivity).fragmentReplace(CalendarViewerFragment(TYPE_AGENDA_SCHOOL_AGENDA), -1)
            }
        }

        calendarItem.removeButton.visibility = View.GONE

        rootView.userCalendarLayout.addView(calendarItem.root)
    }

    private fun initCalendars(user: User){
        addSchoolAgenda()
        if(user.calendarIds.isEmpty()) {
            activity?.runOnUiThread {
                (activity as SAESActivity).getProgressBar()?.visibility = View.GONE
                (activity as SAESActivity).showEmptyText("Presiona + para crear tu propia agenda")
            }

            return
        }
        getUserCalendars(user.calendarIds){ snap ->
            activity?.runOnUiThread {
                (activity as SAESActivity).getProgressBar()?.visibility = View.GONE
            }

            if(snap.isNotEmpty() && activity != null){
                for (doc in snap){
                    val calendarItem = ViewUserCalendarItemBinding.inflate(LayoutInflater.from(context))

                    calendarItem.calendarTitleTextView.text = doc.name
                    calendarItem.calendarTypeTextView.text = if(!doc.private){
                        "Grupal"
                    }else{
                        "Personal"
                    }

                    if(!doc.private){
                        calendarItem.calendarCodeTextView.text = "Código: ${doc.code}"
                    }else{
                        calendarItem.calendarCodeTextView.visibility = View.GONE
                    }

                    calendarItem.calendarButton.setOnClickListener {
                        if(activity is SAESActivity){
                            (activity as SAESActivity).fragmentReplace(CalendarViewerFragment(doc.code, doc.admin), -1)
                        }
                    }

                    calendarItem.removeButton.setOnClickListener {
                        val alertDialog = AlertDialog.Builder(activity, R.style.DialogAlert)

                        alertDialog.setTitle("Borrar ${doc.name}")
                        alertDialog.setMessage("¿Desea borrar esta agenda?")
                        alertDialog.setPositiveButton("Borrar"){ _, _ ->
                            removeCalendar(activity, doc.code).addOnSuccessListener {
                                if(activity is SAESActivity){
                                    (activity as SAESActivity).postNavigationItemSelected(R.id.nav_personal_agenda, false)
                                }
                            }
                        }
                        alertDialog.setNegativeButton("Cancelar", null)

                        alertDialog.show()
                    }

                    activity?.runOnUiThread {
                        rootView.userCalendarLayout.addView(calendarItem.root)
                    }
                }
            }
        }
    }
}