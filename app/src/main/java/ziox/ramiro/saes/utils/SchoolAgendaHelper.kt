package ziox.ramiro.saes.utils

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.databases.AgendaEvent
import ziox.ramiro.saes.databases.AppLocalDatabase
import ziox.ramiro.saes.databases.TYPE_AGENDA_ESCOLAR
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Creado por Ramiro el 1/14/2019 a las 5:40 PM para SAESv2.
 */
class SchoolAgendaHelper (val context: Context) {
    private val firebaseCrashlytics = FirebaseCrashlytics.getInstance()
    data class SchoolAgendaData(
        val title: String,
        val start: Date,
        val finish: Date,
        val isWorkingDay: Boolean
    )

    private val agendaList = ArrayList<SchoolAgendaData>()
    val agendaDao = AppLocalDatabase.getInstance(context).agendaDao()
    private var eventsLambda: (events : List<SchoolAgendaData>) -> Unit = {}
    private var isFetchFinished = false

    init {
        val agendaInterface = JSI()
        if (context.isNetworkAvailable()){
            val agendaWebView = createWebView(context, object : WebViewClient() {
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
            }, null)

            agendaWebView.addJavascriptInterface(agendaInterface, "JSI")
            agendaWebView.loadUrl(getUrl(context) + "Academica/agenda_escolar.aspx")
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

    private inner class JSI {
        @JavascriptInterface
        fun addEvent(
            title: String,
            start: String,
            finish: String,
            isWorkingDay: Boolean,
            isLastItem: Boolean
        ) {
            agendaList.add(SchoolAgendaData(title, stringToDate(start), stringToDate(finish), isWorkingDay))

            if(context.isNetworkAvailable()){
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
                eventsLambda(agendaList)
                (context as SAESActivity).updateWidgets()
            }
        }

        @JavascriptInterface
        fun onEmptyLayout() {
            eventsLambda(agendaList)
        }
    }

    fun getEvents(eventsLambda: (events : List<SchoolAgendaData>) -> Unit){
        this.eventsLambda = eventsLambda
        if (isFetchFinished){
            eventsLambda(agendaList)
        }
    }
}