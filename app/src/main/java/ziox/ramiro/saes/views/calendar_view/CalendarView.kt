package ziox.ramiro.saes.views.calendar_view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import ziox.ramiro.saes.databinding.ViewCalendarBinding
import ziox.ramiro.saes.databinding.ViewCalendarDayBinding
import ziox.ramiro.saes.utils.addBottomInsetPadding

class CalendarView : FrameLayout, TabLayout.OnTabSelectedListener{
    data class EventData(val title: String, val buildingName: String, val classroomName: String)
    data class HoraData(val hour : String, val meridian: String, val events : ArrayList<EventData> = ArrayList())
    data class ScheduleData(val dayTitle : String, val hours : ArrayList<HoraData> = ArrayList())

    private lateinit var layout : ViewCalendarBinding
    private val data = ArrayList<ScheduleData>()
    private var currentPosition = 0

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    private fun initView(){
        layout = ViewCalendarBinding.inflate(LayoutInflater.from(context), this, true)

        layout.recyclerViewSchedule.addBottomInsetPadding()

        layout.recyclerViewSchedule.layoutManager = LinearLayoutManager(context)
        layout.recyclerViewSchedule.adapter = ScheduleAdapter()

        layout.tabSchedule.addOnTabSelectedListener(this)

        layout.recyclerViewSchedule.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val itemVisiblePosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

                layout.tabSchedule.removeOnTabSelectedListener(this@CalendarView)
                if(layout.recyclerViewSchedule.computeVerticalScrollOffset() == 0){
                    layout.tabSchedule.getTabAt(0)?.select()
                }else if(!layout.recyclerViewSchedule.canScrollVertically(1)){
                    layout.tabSchedule.getTabAt(layout.tabSchedule.tabCount-1)?.select()
                }else if(layout.tabSchedule.selectedTabPosition != itemVisiblePosition){
                    layout.tabSchedule.getTabAt(itemVisiblePosition)?.select()
                }
                layout.tabSchedule.addOnTabSelectedListener(this@CalendarView)
            }
        })
    }

    fun addDay(title: String){
        layout.tabSchedule.addTab(layout.tabSchedule.newTab().setText(title))
        data.add(ScheduleData(title))
    }

    fun addHour(hour: String, meridian: String){
        if(data.isNotEmpty()){
            data.last().hours.add(HoraData(hour, meridian))
        }
    }

    fun addEvent(event : EventData){
        if (data.isNotEmpty()) {
            if (data.last().hours.isNotEmpty()) {
                data.last().hours.last().events.add(event)
            }
        }
    }

    fun clear(){
        data.clear()
        layout.tabSchedule.removeAllTabs()
    }

    fun notifyDataSetChanged(){
        layout.recyclerViewSchedule.adapter?.notifyDataSetChanged()
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
        currentPosition = tab.position
        layout.recyclerViewSchedule.smoothScrollToPosition(tab.position)
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {}

    override fun onTabSelected(tab: TabLayout.Tab) {
        currentPosition = tab.position
        layout.recyclerViewSchedule.smoothScrollToPosition(tab.position)
    }

    fun scrollToPrev(){
        if(currentPosition - 1 !in 0 until  layout.tabSchedule.tabCount) return
        layout.tabSchedule.getTabAt(--currentPosition)?.select()
    }

    fun scrollToNext(){
        if(currentPosition + 1 !in 0 until  layout.tabSchedule.tabCount) return
        layout.tabSchedule.getTabAt(++currentPosition)?.select()
    }

    inner class ScheduleAdapter : RecyclerView.Adapter<ScheduleAdapter.ViewAdapter>(){
        inner class ViewAdapter (itemView: ViewCalendarDayBinding) : RecyclerView.ViewHolder(itemView.root){
            val title: TextView = itemView.headerDia
            val horasLayout: LinearLayout = itemView.horasLayout
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewAdapter {
            return ViewAdapter(ViewCalendarDayBinding.inflate(LayoutInflater.from(context), parent, false))
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: ViewAdapter, position: Int) {
            val item = data[position]

            holder.title.text = item.dayTitle


            holder.horasLayout.removeAllViews()
            for(hora in item.hours){
                holder.horasLayout.addView(HourView(context, hora))
            }
        }

        override fun getItemId(position: Int): Long {
            return data[position].dayTitle.hashCode().toLong()
        }
    }
}