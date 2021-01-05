package ziox.ramiro.saes.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.databases.AgendaDao
import ziox.ramiro.saes.databases.AgendaEvent
import ziox.ramiro.saes.databases.AppLocalDatabase
import ziox.ramiro.saes.databases.TYPE_AGENDA_ESCOLAR
import ziox.ramiro.saes.databinding.FragmentAgendaBinding
import ziox.ramiro.saes.databinding.ViewAgendaItemBinding
import ziox.ramiro.saes.utils.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Creado por Ramiro el 1/14/2019 a las 5:40 PM para SAESv2.
 */
class AgendaFragment : Fragment() {
    private val firebaseCrashlytics = FirebaseCrashlytics.getInstance()
    data class AgendaData(
        val title: String,
        val start: String,
        val finish: String,
        val isWorkingDay: Boolean
    )

    lateinit var rootView: FragmentAgendaBinding
    val agendaList = ArrayList<AgendaData>()
    lateinit var agendaDao : AgendaDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = FragmentAgendaBinding.inflate(inflater, container, false)
        agendaDao = AppLocalDatabase.getInstance(requireContext()).agendaDao()
        rootView.agendaRecyclerView.addBottomInsetPadding()

        val agendaInterface = JSI()

        rootView.agendaRecyclerView.layoutManager = LinearLayoutManager(activity)
        rootView.agendaRecyclerView.adapter = AgendaAdapter()

        if (activity?.isNetworkAvailable() == true){
            val agendaWebView = createWebView(activity, object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    agendaDao.deleteAllOfType(TYPE_AGENDA_ESCOLAR)
                    view?.loadUrl(
                        "javascript:" +
                                "const table = document.getElementById(\"ctl00_mainCopy_GVAgenda\");" +
                                "if(table != null){" +
                                "   const row = table.getElementsByTagName(\"tr\");" +
                                "   for(var i = 1 ; i < row.length ; ++i){\n" +
                                "       const col = row[i].getElementsByTagName(\"td\");" +
                                "       window.JSI.addEvent(col[0].innerText, col[1].innerText, col[2].innerText, col[3].innerText == \"Si\", i == row.length-1);" +
                                "   }" +
                                "}else{" +
                                "   window.JSI.onEmptyLayout();" +
                                "}"
                    )
                }
            }, (activity as SAESActivity?)?.getProgressBar())

            agendaWebView.addJavascriptInterface(agendaInterface, "JSI")
            agendaWebView.loadUrl(getUrl(activity) + "Academica/agenda_escolar.aspx")
        }else{
            val data = agendaDao.getAllOfType(TYPE_AGENDA_ESCOLAR)

            if(data.isEmpty()){
                agendaInterface.onEmptyLayout()
            }else{
                for((i, event) in data.withIndex()){
                    agendaInterface.addEvent(
                        event.title,
                        event.start,
                        event.finish,
                        event.isWorkingDay,
                        i == data.lastIndex
                    )
                }
            }
        }

        return rootView.root
    }

    inner class JSI {
        @JavascriptInterface
        fun addEvent(
            title: String,
            start: String,
            finish: String,
            isWorkingDay: Boolean,
            isLastItem: Boolean
        ) {
            agendaList.add(AgendaData(title, start, finish, isWorkingDay))

            if(activity?.isNetworkAvailable() == true){
                agendaDao.insert(AgendaEvent(
                    title,
                    TYPE_AGENDA_ESCOLAR,
                    "EVENTO_INST",
                    "AGENDA_ESCOLAR_FRAGMENT",
                    start,
                    finish,
                    isWorkingDay
                ))
            }

            if (isLastItem) {
                activity?.runOnUiThread {
                    if(activity is SAESActivity){
                        (activity as SAESActivity).updateWidgets()
                    }
                    rootView.agendaRecyclerView.adapter?.notifyDataSetChanged()
                }
            }
        }

        @JavascriptInterface
        fun onEmptyLayout() {
            if (activity is SAESActivity) {
                (activity as SAESActivity).showEmptyText("No hay eventos en la agenda")
            }
        }
    }

    inner class AgendaAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return EventViewHolder(ViewAgendaItemBinding.inflate(layoutInflater, parent, false))
        }

        override fun getItemCount(): Int {
            return agendaList.size
        }

        private fun stringToDate(str: String): Date {
            val str2 = str.replace(". ", "").split(" ").mapIndexed { index, s ->
                if (index == 0) {
                    (mesToInt(s) + 1).toString()
                } else s
            }
            val format = SimpleDateFormat("M d yyyy", Locale.US)

            try {
                return format.parse(str2.joinToString(" "))!!
            } catch (e: Exception) {
                Log.e(this.javaClass.canonicalName, e.toString())
            }
            return Date()
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val event = agendaList[position]
            if (holder is EventViewHolder) {
                holder.title.text = event.title
                holder.isWorkingDay.text = if (event.isWorkingDay) {
                    "Laborable"
                } else {
                    "No laborable"
                }

                holder.addToPersonalCalendarButton.setOnClickListener {
                    firebaseCrashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                    if (activity != null) {
                        val intent = Intent(Intent.ACTION_INSERT)
                            .setData(CalendarContract.Events.CONTENT_URI)
                            .putExtra(CalendarContract.Events.TITLE, event.title)
                            .putExtra(
                                CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                stringToDate(event.start).time
                            )
                            .putExtra(
                                CalendarContract.EXTRA_EVENT_END_TIME,
                                stringToDate(event.finish).time
                            )

                        if (intent.resolveActivity(requireContext().packageManager) != null)
                            startActivity(intent)
                        else
                            Toast.makeText(
                                activity,
                                "No hay aplicaciones que puedan ejecutar esta accion",
                                Toast.LENGTH_SHORT
                            ).show()
                    }
                }
            }
        }

        inner class EventViewHolder constructor(agendaItemBinding: ViewAgendaItemBinding) : RecyclerView.ViewHolder(agendaItemBinding.root) {
            val title = agendaItemBinding.titleTextView
            val isWorkingDay = agendaItemBinding.workingDayTextView
            val addToPersonalCalendarButton = agendaItemBinding.addToPersonalCalendarButton
        }
    }
}