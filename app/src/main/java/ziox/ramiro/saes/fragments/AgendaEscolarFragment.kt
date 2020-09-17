package ziox.ramiro.saes.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.fragment_agenda_escolar.view.*
import kotlinx.android.synthetic.main.view_agenda_escolar_item.view.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.databases.AgendaEscolarDatabase
import ziox.ramiro.saes.utils.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Creado por Ramiro el 1/14/2019 a las 5:40 PM para SAESv2.
 */
class AgendaEscolarFragment : Fragment() {
    private val crashlytics = FirebaseCrashlytics.getInstance()
    data class AgendaData(
        val nombre: String,
        val inicio: String,
        val final: String,
        val laborable: Boolean
    )

    lateinit var rootView: View
    val agendaList = ArrayList<AgendaData>()
    lateinit var agendaDatabase : AgendaEscolarDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_agenda_escolar, container, false)
        agendaDatabase = AgendaEscolarDatabase(activity)
        rootView.agendaRecyclerView.addBottomInsetPadding()

        val agendaInterface = JSI()

        rootView.agendaRecyclerView.layoutManager = LinearLayoutManager(activity)
        rootView.agendaRecyclerView.adapter = AgendaAdapter()

        if (activity?.isNetworkAvailable() == true){
            val agendaWebView = createWebView(activity, object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    agendaDatabase.removeOnlyType(AgendaEscolarDatabase.TYPE_AGENDA_ESCOLAR)
                    view?.loadUrl(
                        "javascript:" +
                                "const table = document.getElementById(\"ctl00_mainCopy_GVAgenda\");" +
                                "if(table != null){" +
                                "   const row = table.getElementsByTagName(\"tr\");" +
                                "   for(var i = 1 ; i < row.length ; ++i){\n" +
                                "       const col = row[i].getElementsByTagName(\"td\");" +
                                "       window.JSI.addEvento(col[0].innerText, col[1].innerText, col[2].innerText, col[3].innerText == \"Si\", i == row.length-1);" +
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
            val data = agendaDatabase.getOnlyType(AgendaEscolarDatabase.TYPE_AGENDA_ESCOLAR)

            if(data.count == 0){
                agendaInterface.onEmptyLayout()
            }else{
                while (data.moveToNext()){
                    val evento = AgendaEscolarDatabase.cursorAsClaseData(data)
                    agendaInterface.addEvento(
                        evento.nombre,
                        evento.inicio,
                        evento.final,
                        evento.laborable != 0,
                        data.isLast
                    )
                }
            }
        }

        return rootView
    }

    inner class JSI {
        @JavascriptInterface
        fun addEvento(
            nombre: String,
            inicio: String,
            final: String,
            laborable: Boolean,
            last: Boolean
        ) {
            agendaList.add(AgendaData(nombre, inicio, final, laborable))

            if(activity?.isNetworkAvailable() == true){
                agendaDatabase.addEvento(
                    AgendaEscolarDatabase.Data(nombre, AgendaEscolarDatabase.TYPE_AGENDA_ESCOLAR, "EVENTO_INST","AGENDA_ESCOLAR_FRAGMENT", inicio, final, if (laborable) 1 else 0
                ))
            }

            if (last) {
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
            return EventoViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.view_agenda_escolar_item,
                    parent,
                    false
                )
            )
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

            }
            return Date()
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val evento = agendaList[position]
            if (holder is EventoViewHolder) {
                holder.titulo.text = evento.nombre
                holder.laborable.text = if (evento.laborable) {
                    "Laborable"
                } else {
                    "No laborable"
                }

                holder.btnCalendar.setOnClickListener {
                    crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                    if (activity != null) {
                        val intent = Intent(Intent.ACTION_INSERT)
                            .setData(CalendarContract.Events.CONTENT_URI)
                            .putExtra(CalendarContract.Events.TITLE, evento.nombre)
                            .putExtra(
                                CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                stringToDate(evento.inicio).time
                            )
                            .putExtra(
                                CalendarContract.EXTRA_EVENT_END_TIME,
                                stringToDate(evento.final).time
                            )

                        if (intent.resolveActivity(activity!!.packageManager) != null)
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

        inner class EventoViewHolder constructor(v: View) : RecyclerView.ViewHolder(v) {
            var titulo = TextView(this@AgendaEscolarFragment.activity)
            var laborable = TextView(this@AgendaEscolarFragment.activity)
            var btnCalendar = Button(this@AgendaEscolarFragment.activity)

            init {
                titulo = v.agendaNombre
                laborable = v.agendaLaborable
                btnCalendar = v.agendaCalendarioBtn
            }
        }
    }
}