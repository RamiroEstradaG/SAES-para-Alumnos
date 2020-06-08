package ziox.ramiro.saes.views.scheduleview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.view_schedule.view.*
import kotlinx.android.synthetic.main.view_schedule_dia.view.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.utils.addBottomInsetPadding

class ScheduleView : FrameLayout, TabLayout.OnTabSelectedListener{
    data class EventData(val title: String, val edificio: String, val salon: String)
    data class HoraData(val hora : String, val meridian: String, val eventos : ArrayList<EventData> = ArrayList())
    data class ScheduleData(val diaTitle : String, val horas : ArrayList<HoraData> = ArrayList())

    private lateinit var layout : View
    private val data = ArrayList<ScheduleData>()
    private var currentPosition = 0

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    private fun initView(){
        layout = LayoutInflater.from(context).inflate(R.layout.view_schedule, this, true)

        layout.recyclerViewSchedule.addBottomInsetPadding()

        layout.recyclerViewSchedule.layoutManager = LinearLayoutManager(context)
        layout.recyclerViewSchedule.adapter = ScheduleAdapter()

        layout.tabSchedule.addOnTabSelectedListener(this)

        layout.recyclerViewSchedule.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val itemVisiblePosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

                layout.tabSchedule.removeOnTabSelectedListener(this@ScheduleView)
                if(layout.recyclerViewSchedule.computeVerticalScrollOffset() == 0){
                    layout.tabSchedule.getTabAt(0)?.select()
                }else if(!layout.recyclerViewSchedule.canScrollVertically(1)){
                    layout.tabSchedule.getTabAt(layout.tabSchedule.tabCount-1)?.select()
                }else if(layout.tabSchedule.selectedTabPosition != itemVisiblePosition){
                    layout.tabSchedule.getTabAt(itemVisiblePosition)?.select()
                }
                layout.tabSchedule.addOnTabSelectedListener(this@ScheduleView)
            }
        })
    }

    fun addDay(title: String){
        layout.tabSchedule.addTab(layout.tabSchedule.newTab().setText(title))
        data.add(ScheduleData(title))
    }

    fun addHour(hour: String, meridian: String){
        if(data.isNotEmpty()){
            data.last().horas.add(HoraData(hour, meridian))
        }
    }

    fun addEvent(event : EventData){
        if (data.isNotEmpty()) {
            if (data.last().horas.isNotEmpty()) {
                data.last().horas.last().eventos.add(event)
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
        inner class ViewAdapter (itemView: View) : RecyclerView.ViewHolder(itemView){
            val title: TextView = itemView.headerDia
            val horasLayout: LinearLayout = itemView.horasLayout
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewAdapter {
            return ViewAdapter(LayoutInflater.from(context).inflate(R.layout.view_schedule_dia, parent, false))
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: ViewAdapter, position: Int) {
            val item = data[position]

            holder.title.text = item.diaTitle


            holder.horasLayout.removeAllViews()
            for(hora in item.horas){
                holder.horasLayout.addView(HoraView(context, hora))
            }
        }

        override fun getItemId(position: Int): Long {
            return data[position].diaTitle.hashCode().toLong()
        }
    }
}