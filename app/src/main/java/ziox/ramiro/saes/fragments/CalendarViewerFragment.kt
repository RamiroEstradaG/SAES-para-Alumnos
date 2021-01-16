package ziox.ramiro.saes.fragments

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.util.rangeTo
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.bottomappbar.BottomAppBar
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.databases.*
import ziox.ramiro.saes.databinding.FragmentCalendarViewerBinding
import ziox.ramiro.saes.databinding.ViewUserCalendarAdItemBinding
import ziox.ramiro.saes.databinding.ViewUserCalendarInfoItemBinding
import ziox.ramiro.saes.dialogs.AddCalendarEventDialogFragment
import ziox.ramiro.saes.utils.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

const val TYPE_AGENDA_SCHOOL_AGENDA = "SCHOOL_AGENDA_TYPE_ID"

class CalendarViewerFragment (private val code: String, private val admins: List<String> = listOf()): Fragment() {
    private val events = ArrayList<CalendarEvent>()
    private val showingEvents = ArrayList<CalendarEvent?>()
    private var showingDate = Calendar.getInstance().time
    private var isShowingMonth = false
    lateinit var rootView : FragmentCalendarViewerBinding
    private lateinit var colors : Array<String>
    private lateinit var eventTypes : Array<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = FragmentCalendarViewerBinding.inflate(inflater, container, false)
        setSystemUiLightStatusBar(requireActivity(), false)

        val adapter = CalendarEventsAdapter()
        adapter.setHasStableIds(true)
        rootView.eventListRecyclerView.addBottomInsetPadding()
        rootView.eventListRecyclerView.layoutManager = LinearLayoutManager(activity)
        rootView.eventListRecyclerView.adapter = adapter
        colors = requireContext().resources.getStringArray(R.array.paletaHorario)
        eventTypes = requireContext().resources.getStringArray(R.array.tipo_eventos)

        if(admins.contains(getHashUserId(context))){
            if(activity is SAESActivity){
                (activity as SAESActivity).showFab(R.drawable.ic_add_black_24dp, {
                    AddCalendarEventDialogFragment(code).show(childFragmentManager, "add_event_calendario_trabajo")
                }, BottomAppBar.FAB_ALIGNMENT_MODE_END)
            }
        }

        rootView.calendarView.setListener(object : CompactCalendarView.CompactCalendarViewListener{
            override fun onDayClick(dateClicked: Date) {
                updateShowingEvents(dateClicked)
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                activity?.runOnUiThread {
                    val cal = firstDayOfNewMonth.toCalendar()
                    rootView.monthYearDisplay.text = "${MES_COMPLETO[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
                    updateShowingEvents(firstDayOfNewMonth, false)
                }
            }

        })

        rootView.calendarView.setFirstDayOfWeek(Calendar.SUNDAY)

        (activity as? SAESActivity)?.setOnDragHorizontaly {
            if(it){
                rootView.calendarView.scrollLeft()
            }else{
                rootView.calendarView.scrollRight()
            }
        }

        if (code == TYPE_AGENDA_SCHOOL_AGENDA){
            initSchoolAgenda()
        }else{
            initCalendarData()
        }

        activity?.runOnUiThread {
            val cal = rootView.calendarView.firstDayOfCurrentMonth.toCalendar()
            rootView.monthYearDisplay.text = "${MES_COMPLETO[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
        }

        return rootView.root
    }

    private fun Long.toDays() = this.div(86400000.0)
    private fun Double.toMillis() = this.times(86400000.0).toLong()

    private fun initSchoolAgenda(){
        SchoolAgendaHelper(requireContext()).getEvents { agendaEvents ->
            activity?.runOnUiThread {
                this.events.clear()
                for ((i, agendaData) in agendaEvents.withIndex()){
                    val startCalendar = agendaData.start.toCalendar()
                    val finishCalendar = agendaData.finish.toCalendar()
                    val dayRange = startCalendar.timeInMillis.toDays().toLong()..finishCalendar.timeInMillis.toDays().toLong()
                    for ((e, day) in dayRange.withIndex()){
                        this.events.add(CalendarEvent(
                            when(day){
                                dayRange.first -> startCalendar.timeInMillis
                                dayRange.last -> finishCalendar.timeInMillis
                                else -> day.toDouble().toMillis()
                            },
                            agendaData.title,
                            getSchoolName(context),
                            "Académico",
                            "",
                            true,
                            "",
                            "${i}_$e"
                        ))
                    }
                }
                rootView.calendarView.removeAllEvents()
                this.updateEvents()
                this.updateShowingEvents(this.showingDate, isShowingMonth)
            }
        }
    }

    private fun initCalendarData(){
        getEvents(code){
            activity?.runOnUiThread {
                this.events.clear()
                this.events.addAll(it)
                rootView.calendarView.removeAllEvents()
                this.updateEvents()
                this.updateDatabase(it)
                this.updateShowingEvents(this.showingDate, isShowingMonth)
            }
        }
    }

    private fun updateDatabase(events: List<CalendarEvent>){
        val agenda = AppLocalDatabase.getInstance(requireContext()).agendaDao()
        agenda.deleteAllOfGroup(code)
        for (event in events){
            val date = Calendar.getInstance()
            date.timeInMillis = event.date
            val format = SimpleDateFormat("MMM dd yyyy", Locale("es", "MX")).format(date.time)
            agenda.insert(
                AgendaEvent(
                    event.title,
                    TYPE_CALENDARIO_TRABAJO,
                    event.type,
                    code,
                    format,
                    format,
                    true
                )
            )
        }
        if(activity is SAESActivity){
            (activity as SAESActivity).updateWidgets()
        }
    }

    private fun updateEvents(){
        rootView.calendarView.addEvents(events.map {
            val index = eventTypes.indexOf(it.type)

            return@map Event(
                if(index >= 0){
                    Color.parseColor(colors[index])
                }else{
                    ContextCompat.getColor(requireContext(), R.color.colorTextPrimary)
                },
                it.date, it)
        })
    }

    private fun updateShowingEvents(date: Date, isMonth : Boolean = false){
        this.showingDate = date
        this.isShowingMonth = isMonth

        showingEvents.clear()
        if(context?.haveDonated() == false){
            showingEvents.add(null)
        }
        showingEvents.addAll(if(!isMonth){
            rootView.calendarView.getEvents(date).map {
                it.data as CalendarEvent
            }
        }else{
            rootView.calendarView.getEventsForMonth(date).map {
                it.data as CalendarEvent
            }
        })

        rootView.eventListRecyclerView.adapter?.notifyDataSetChanged()
    }

    inner class CalendarEventsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private lateinit var adViewHolder : AdHolder

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if(viewType == 0){
                if(!::adViewHolder.isInitialized){
                    adViewHolder = AdHolder(ViewUserCalendarAdItemBinding.inflate(layoutInflater, parent, false))
                    adViewHolder.adView.loadAd(AdRequest.Builder().build())
                }
                adViewHolder
            }else{
                ViewHolder(ViewUserCalendarInfoItemBinding.inflate(layoutInflater, parent, false))
            }
        }

        override fun getItemId(position: Int): Long {
            return if(showingEvents[position] == null){
                HashUtils.sha1("AD_ID").hashCode().toLong()
            }else{
                HashUtils.sha1(showingEvents[position]!!.id).hashCode().toLong()
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if(showingEvents[position] == null){
                0
            }else{
                1
            }
        }

        override fun getItemCount() = showingEvents.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(holder is ViewHolder){
                val data = showingEvents[position]!!
                val calendar = Calendar.getInstance()

                calendar.timeInMillis = data.date

                holder.title.text = data.title
                holder.type.text = data.type

                val index = eventTypes.indexOf(data.type)
                holder.type.setTextColor(if(index >= 0){
                    Color.parseColor(colors[index])
                }else{
                    ContextCompat.getColor(requireContext(), R.color.colorTextPrimary)
                })

                if(data.courseName.isNotBlank()){
                    holder.courseName.text = data.courseName
                }else{
                    holder.courseName.visibility = View.GONE
                }
                holder.info.text = data.info
                holder.date.text = calendar.format()

                if (code == TYPE_AGENDA_SCHOOL_AGENDA){
                    holder.infoLayout.visibility = View.GONE
                }

                if(admins.contains(getHashUserId(activity))){
                    holder.removeButton.setOnClickListener {
                        val alertDialog = AlertDialog.Builder(activity, R.style.DialogAlert)

                        alertDialog.setTitle("Borrar ${data.title}")
                        alertDialog.setMessage("¿Desea borrar este elemento?")
                        alertDialog.setPositiveButton("Borrar"){ _, _ ->
                            removeEvent(code, data.id)
                        }
                        alertDialog.setNegativeButton("Cancelar", null)

                        alertDialog.show()
                    }

                    holder.editButton.setOnClickListener {
                        AddCalendarEventDialogFragment(code, data).show(childFragmentManager, "edit_evento_trabajo")
                    }
                }else{
                    holder.removeButton.visibility = View.GONE
                    holder.editButton.visibility = View.GONE
                }
            }
        }

        inner class ViewHolder(itemBinding: ViewUserCalendarInfoItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
            val title: TextView = itemBinding.eventTitleTextView
            val type: TextView = itemBinding.eventTypeTextView
            val courseName: TextView = itemBinding.eventCourseNameTextView
            val info: TextView = itemBinding.eventInfoTextView
            val date: TextView = itemBinding.eventDateTextView
            val removeButton: ImageView = itemBinding.removeButton
            val editButton : ImageView = itemBinding.editButton
            val infoLayout = itemBinding.infoLayout
        }

        inner class AdHolder(adItemBinding: ViewUserCalendarAdItemBinding) : RecyclerView.ViewHolder(adItemBinding.root) {
            val adView : AdView = adItemBinding.userCalendarAdView
            val parent : FrameLayout = adItemBinding.parentAdItem
        }

        inner class EmptyHolder(v: View) : RecyclerView.ViewHolder(v)
    }
}