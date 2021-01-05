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

class CalendarViewerFragment (private val codigo: String, private val admin: List<String>): Fragment() {
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
        setLightStatusBar(activity)

        val adapter = CalendarEventsAdapter()
        adapter.setHasStableIds(true)
        rootView.eventListRecyclerView.addBottomInsetPadding()
        rootView.eventListRecyclerView.layoutManager = LinearLayoutManager(activity)
        rootView.eventListRecyclerView.adapter = adapter
        colors = requireContext().resources.getStringArray(R.array.paletaHorario)
        eventTypes = requireContext().resources.getStringArray(R.array.tipo_eventos)

        if(admin.contains(getHashUserId(context))){
            if(activity is SAESActivity){
                (activity as SAESActivity).showFab(R.drawable.ic_add_black_24dp, {
                    AddCalendarEventDialogFragment(codigo).show(childFragmentManager, "add_event_calendario_trabajo")
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

        (activity as? SAESActivity)?.setOnDragHorizontaly {
            if(it){
                rootView.calendarView.scrollLeft()
            }else{
                rootView.calendarView.scrollRight()
            }
        }

        initCalendarData()

        activity?.runOnUiThread {
            val cal = rootView.calendarView.firstDayOfCurrentMonth.toCalendar()
            rootView.monthYearDisplay.text = "${MES_COMPLETO[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
        }

        return rootView.root
    }

    private fun initCalendarData(){
        getEvents(codigo){
            this.events.clear()
            this.events.addAll(it)
            rootView.calendarView.removeAllEvents()
            this.updateEvents()
            this.updateDatabase(it)
            this.updateShowingEvents(this.showingDate, isShowingMonth)
        }
    }

    private fun updateDatabase(events: List<CalendarEvent>){
        val agenda = AppLocalDatabase.getInstance(requireContext()).agendaDao()
        agenda.deleteAllOfGroup(codigo)
        for (event in events){
            val date = Calendar.getInstance()
            date.timeInMillis = event.date
            val format = SimpleDateFormat("MMM dd yyyy", Locale("es", "MX")).format(date.time)
            agenda.insert(
                AgendaEvent(
                    event.title,
                    TYPE_CALENDARIO_TRABAJO,
                    event.type,
                    codigo,
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
                Color.parseColor(colors[if(index >= 0){
                    index
                }else{
                    colors.lastIndex
                }]),
                it.date, it)
        })
    }

    private fun updateShowingEvents(date: Date, isMonth : Boolean = false){
        this.showingDate = date
        this.isShowingMonth = isMonth

        showingEvents.clear()
        showingEvents.add(null)
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
            return if(context?.haveDonated() == true && viewType == 0){
                EmptyHolder(View(context))
            }else if(viewType == 0){
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
            return if(position == 0){
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
                holder.type.setTextColor(Color.parseColor(colors[if(index >= 0){
                    index
                }else{
                    colors.lastIndex
                }]))

                if(data.courseName.isNotBlank()){
                    holder.courseName.text = data.courseName
                }else{
                    holder.courseName.visibility = View.GONE
                }
                holder.info.text = data.info
                holder.date.text = calendar.format()

                if(admin.contains(getHashUserId(activity))){
                    holder.removeButton.setOnClickListener {
                        val alertDialog = AlertDialog.Builder(activity, R.style.DialogAlert)

                        alertDialog.setTitle("Borrar ${data.title}")
                        alertDialog.setMessage("¿Desea borrar este elemento?")
                        alertDialog.setPositiveButton("Borrar"){ _, _ ->
                            removeEvent(codigo, data.id)
                        }
                        alertDialog.setNegativeButton("Cancelar", null)

                        alertDialog.show()
                    }

                    holder.editButton.setOnClickListener {
                        AddCalendarEventDialogFragment(codigo, data).show(childFragmentManager, "edit_evento_trabajo")
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
        }

        inner class AdHolder(adItemBinding: ViewUserCalendarAdItemBinding) : RecyclerView.ViewHolder(adItemBinding.root) {
            val adView : AdView = adItemBinding.userCalendarAdView
            val parent : FrameLayout = adItemBinding.parentAdItem
        }

        inner class EmptyHolder(v: View) : RecyclerView.ViewHolder(v)
    }
}