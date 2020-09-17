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
import kotlinx.android.synthetic.main.fragment_calendario_trabajo_viewer.*
import kotlinx.android.synthetic.main.fragment_calendario_trabajo_viewer.view.*
import kotlinx.android.synthetic.main.view_calendario_ad_item.view.*
import kotlinx.android.synthetic.main.view_calendario_info_item.view.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.dialogs.AddEventoCalendarioTrabajoDialog
import ziox.ramiro.saes.databases.AgendaEscolarDatabase
import ziox.ramiro.saes.databases.Evento
import ziox.ramiro.saes.databases.getEventos
import ziox.ramiro.saes.databases.removeEvento
import ziox.ramiro.saes.utils.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CalendarioTrabajoViewerFragment (private val codigo: String, private val admin: List<String>): Fragment() {
    private val events = ArrayList<Evento>()
    private val showingEvents = ArrayList<Evento?>()
    private var showingDate = Calendar.getInstance().time
    private var isShowingMonth = false
    lateinit var rootView : View
    private lateinit var colores : Array<String>
    private lateinit var tipoEventos : Array<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_calendario_trabajo_viewer, container, false)
        setLightStatusBar(activity)

        val adapter = EventoAdapter()
        adapter.setHasStableIds(true)
        rootView.eventosRecyclerView.addBottomInsetPadding()
        rootView.eventosRecyclerView.layoutManager = LinearLayoutManager(activity)
        rootView.eventosRecyclerView.adapter = adapter
        colores = activity!!.resources.getStringArray(R.array.paletaHorario)
        tipoEventos = activity!!.resources.getStringArray(R.array.tipo_eventos)

        if(admin.contains(getHashUserId(context))){
            if(activity is SAESActivity){
                (activity as SAESActivity).showFab(R.drawable.ic_add_black_24dp, View.OnClickListener {
                    AddEventoCalendarioTrabajoDialog(codigo).show(childFragmentManager, "add_event_calendario_trabajo")
                }, BottomAppBar.FAB_ALIGNMENT_MODE_END)
            }
        }

        rootView.compactCalendarView.setListener(object : CompactCalendarView.CompactCalendarViewListener{
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
                rootView.compactCalendarView.scrollLeft()
            }else{
                rootView.compactCalendarView.scrollRight()
            }
        }

        initCalendarData()

        activity?.runOnUiThread {
            val cal = rootView.compactCalendarView.firstDayOfCurrentMonth.toCalendar()
            rootView.monthYearDisplay.text = "${MES_COMPLETO[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
        }

        return rootView
    }

    private fun initCalendarData(){
        getEventos(codigo){
            this.events.clear()
            this.events.addAll(it)
            this.compactCalendarView?.removeAllEvents()
            this.updateEventos()
            this.updateDatabase(it)
            this.updateShowingEvents(this.showingDate, isShowingMonth)
        }
    }

    private fun updateDatabase(eventos: List<Evento>){
        val agenda = AgendaEscolarDatabase(activity)
        agenda.removeOnlyGrupo(codigo)
        for (e in eventos){
            val dia = Calendar.getInstance()
            dia.timeInMillis = e.dia
            val format = SimpleDateFormat("MMM dd yyyy", Locale("es", "MX")).format(dia.time)
            agenda.addEvento(AgendaEscolarDatabase.Data(
                e.titulo,
                AgendaEscolarDatabase.TYPE_CALENDARIO_TRABAJO,
                e.tipo,
                codigo,
                format,
                format,
                1
            ))
        }
        if(activity is SAESActivity){
            (activity as SAESActivity).updateWidgets()
        }
        agenda.close()
    }

    private fun updateEventos(){
        rootView.compactCalendarView.addEvents(events.map {
            val index = tipoEventos.indexOf(it.tipo)

            return@map Event(
                Color.parseColor(colores[if(index >= 0){
                    index
                }else{
                    colores.lastIndex
                }]),
                it.dia, it)
        })
    }

    private fun updateShowingEvents(date: Date, isMonth : Boolean = false){
        this.showingDate = date
        this.isShowingMonth = isMonth

        showingEvents.clear()
        showingEvents.add(null)
        showingEvents.addAll(if(!isMonth){
            rootView.compactCalendarView.getEvents(date).map {
                it.data as Evento
            }
        }else{
            rootView.compactCalendarView.getEventsForMonth(date).map {
                it.data as Evento
            }
        })

        rootView.eventosRecyclerView.adapter?.notifyDataSetChanged()
    }

    inner class EventoAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private lateinit var adViewHolder : AdHolder

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if(context?.haveDonated() == true && viewType == 0){
                EmptyHolder(View(context))
            }else if(viewType == 0){
                if(!::adViewHolder.isInitialized){
                    adViewHolder = AdHolder(
                        LayoutInflater.from(parent.context).inflate(
                            R.layout.view_calendario_ad_item,
                            parent,
                            false
                        )
                    )

                    adViewHolder.adView.loadAd(AdRequest.Builder().build())
                }


                adViewHolder
            }else{
                ViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.view_calendario_info_item,
                        parent,
                        false
                    )
                )
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

                calendar.timeInMillis = data.dia

                holder.titulo.text = data.titulo
                holder.tipo.text = data.tipo

                val index = tipoEventos.indexOf(data.tipo)
                holder.tipo.setTextColor(Color.parseColor(colores[if(index >= 0){
                    index
                }else{
                    colores.lastIndex
                }]))

                if(data.materia.isNotBlank()){
                    holder.materia.text = data.materia
                }else{
                    holder.materia.visibility = View.GONE
                }
                holder.info.text = data.info
                holder.fecha.text = calendar.format()

                if(admin.contains(getHashUserId(activity))){
                    holder.remove.setOnClickListener {
                        val alertDialog = AlertDialog.Builder(activity, R.style.DialogAlert)

                        alertDialog.setTitle("Borrar ${data.titulo}")
                        alertDialog.setMessage("¿Desea borrar este elemento?")
                        alertDialog.setPositiveButton("Borrar"){ _, _ ->
                            removeEvento(codigo, data.id)
                        }
                        alertDialog.setNegativeButton("Cancelar", null)

                        alertDialog.show()
                    }

                    holder.edit.setOnClickListener {
                        AddEventoCalendarioTrabajoDialog(codigo, data).show(childFragmentManager, "edit_evento_trabajo")
                    }
                }else{
                    holder.remove.visibility = View.GONE
                    holder.edit.visibility = View.GONE
                }
            }
        }

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val titulo: TextView = v.itemCalendarioTitulo
            val tipo: TextView = v.itemCalendarioTipo
            val materia: TextView = v.itemCalendarioMateria
            val info: TextView = v.itemCalendarioInfo
            val fecha: TextView = v.itemCalendarioFecha
            val remove: ImageView = v.removeEvento
            val edit : ImageView = v.editEvento
        }

        inner class AdHolder(v: View) : RecyclerView.ViewHolder(v) {
            val adView : AdView = v.adViewCalendarioTrabajo
            val parent : FrameLayout = v.parentAdItem
        }

        inner class EmptyHolder(v: View) : RecyclerView.ViewHolder(v)
    }
}