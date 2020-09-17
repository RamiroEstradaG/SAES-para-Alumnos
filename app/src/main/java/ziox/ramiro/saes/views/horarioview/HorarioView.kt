package ziox.ramiro.saes.views.horarioview

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.text.Editable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.view_horario.view.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.dialogs.AnadirMateriaDialog
import ziox.ramiro.saes.fragments.HorarioFragment
import ziox.ramiro.saes.databases.CorreccionHorarioDatabase
import ziox.ramiro.saes.databases.HorarioDatabase
import ziox.ramiro.saes.databases.HorarioGeneradoDatabase
import ziox.ramiro.saes.databases.HorarioPersonalDatabase
import ziox.ramiro.saes.utils.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Creado por Ramiro el 11/11/2018 a las 4:29 PM para SAESv2.
 */

@Suppress("DEPRECATION")
class HorarioView : FrameLayout {
    private val crashlytics = FirebaseCrashlytics.getInstance()
    companion object {
        const val JsiName = "JSI"
    }

    private enum class BottomSheetEditType{
        EDIT_CLASS, NEW_CLASS
    }

    private lateinit var bottomSheet: BottomSheetBehavior<LinearLayout>
    lateinit var lay: View

    private lateinit var horarioDatabase: HorarioDatabase
    private lateinit var correccionHorarioDatabase: CorreccionHorarioDatabase
    private lateinit var horarioPersonalDatabase: HorarioPersonalDatabase
    private val diasLayout = ArrayList<FrameLayout>()
    lateinit var jsi: JSI
        private set

    private var diasName: ArrayList<String> = arrayListOf(
        "Lunes",
        "Martes",
        "Miercoles",
        "Jueves",
        "Viernes"
    )
    private val onExpandedListeners = ArrayList<(isExpanded: Boolean, index: Int) -> Unit>()
    private val clasesData = ArrayList<ClaseData>()
    val webView: WebView

    var minHora = 25.0
    var maxHora = -1.0
    var claseSize = 70
    var offsetBottom = 0
    var canEdit = false
    var isDatabaseEnabled = false
    var isCurrentDayVisible = false
    private var isCurrentDayOpenOnStartEnable = false
    private var bottomSheetType = BottomSheetEditType.EDIT_CLASS
    var isExpanded = false
    var isCurrentHourVisible = false
    var selectedIndex = 0
    private var hasClass = false

    private var activity: FragmentActivity? = null
    private var jSIadded = false
    private var editingClase: ClaseData? = null
    private var currentDiaIndex = -1

    constructor(context: Context) : super(context) {
        webView = createWebView(context, WebViewClient(), null)
        initLayout()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        webView = createWebView(context, WebViewClient(), null)
        initLayout()
    }

    private fun changeBottomSheetState(state: Int) {
        if (state == BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheet.state = state
        } else {
            lay.editClaseBottomSheet.visibility = View.VISIBLE
            Handler().postDelayed({
                bottomSheet.state = state
            }, 50)
        }
    }

    private fun initLayout() {
        lay = LayoutInflater.from(context).inflate(R.layout.view_horario, this, true)
        lay.bottomSheetScrollHorarioView.addBottomInsetPadding()
        bottomSheet = BottomSheetBehavior.from(lay.editClaseBottomSheet)

        bottomSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}

            override fun onStateChanged(p0: View, p1: Int) {
                if (p1 == BottomSheetBehavior.STATE_HIDDEN) {
                    lay.editClaseBottomSheet.visibility = View.GONE
                    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(this@HorarioView.windowToken, 0)
                    if(activity is SAESActivity){
                        (activity as SAESActivity).showFab()
                    }
                    if(activity?.supportFragmentManager?.fragments?.first() is HorarioFragment){
                        (activity?.supportFragmentManager?.fragments?.first() as HorarioFragment).setPaginationVisible(true)
                    }
                }else if(p1 == BottomSheetBehavior.STATE_EXPANDED){
                    if(activity is SAESActivity){
                        (activity as SAESActivity).hideFab()
                        (activity?.supportFragmentManager?.fragments?.first() as HorarioFragment).setPaginationVisible(false)
                    }
                }
            }
        })

        currentDiaIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2

        changeBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)

        val tran = LayoutTransition()
        tran.enableTransitionType(LayoutTransition.CHANGING)
        tran.setDuration(300)
        lay.diasContainer.layoutTransition = tran

        for (i in 0 until lay.diasContainer.childCount) {
            val dia = lay.diasContainer.getChildAt(i) as FrameLayout
            diasLayout.add(dia)
            initFrame(i)
        }

        if (isCurrentDayOpenOnStartEnable) {
            if (currentDiaIndex in 0..4) {
                toggleDayState(currentDiaIndex)
            }
        }

        lay.cerrarEditBottomSheetButton.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            editingClase = null
            changeBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)
        }

        lay.guardarClaseButton.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            when(bottomSheetType){
                BottomSheetEditType.EDIT_CLASS -> {
                    if (editingClase != null && ::correccionHorarioDatabase.isInitialized && ::horarioDatabase.isInitialized) {
                        val newData = editingClase!!.copy()

                        try {
                            newData.materia = lay.editClaseNombre.editText!!.text.toString()
                            newData.profesor = lay.editClaseProfesor.editText!!.text.toString()
                            newData.edificio = lay.editClaseEdificio.editText!!.text.toString()
                            newData.salon = lay.editClaseSalon.editText!!.text.toString()
                            newData.diaIndex = lay.selectableDia.selectedIndex
                            newData.horaInicio = hourToDouble(lay.editClaseHoraInicio.editText!!.text.toString())
                            newData.horaFinal = hourToDouble(lay.editClaseHoraFinal.editText!!.text.toString())
                        } catch (e: Exception) {
                            Log.e("asd", e.toString())
                        }

                        correccionHorarioDatabase.deleteData(newData)

                        if (correccionHorarioDatabase.add(newData)) {
                            refreshMessage()
                            changeBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)
                        } else {
                            failedMessage()
                        }

                        if (activity is SAESActivity) {
                            (activity as SAESActivity).updateWidgets()
                        }

                        editingClase = null
                    }
                }
                BottomSheetEditType.NEW_CLASS -> {
                    if (::horarioPersonalDatabase.isInitialized && ::horarioDatabase.isInitialized) {
                        val materia = lay.editClaseNombre.editText!!.text.toString()
                        val profesor = lay.editClaseProfesor.editText!!.text.toString()
                        val edificio = lay.editClaseEdificio.editText!!.text.toString()
                        val salon = lay.editClaseSalon.editText!!.text.toString()
                        val grupo = lay.editClaseGrupo.editText!!.text.toString()
                        val diaIndex = lay.selectableDia.selectedIndex
                        val horaInicio = hourToDouble(lay.editClaseHoraInicio.editText!!.text.toString())
                        val horaFinal = hourToDouble(lay.editClaseHoraFinal.editText!!.text.toString())

                        when{
                            materia.isBlank() -> lay.editClaseNombre.error = "Este campo está vacío"
                            profesor.isBlank() -> {
                                lay.editClaseNombre.error = null
                                lay.editClaseProfesor.error = "Este campo está vacío"
                            }
                            edificio.isBlank() -> {
                                lay.editClaseNombre.error = null
                                lay.editClaseProfesor.error = null
                                lay.editClaseEdificio.error = "Este campo está vacío"
                            }
                            salon.isBlank() -> {
                                lay.editClaseNombre.error = null
                                lay.editClaseProfesor.error = null
                                lay.editClaseEdificio.error = null
                                lay.editClaseSalon.error = "Este campo está vacío"
                            }
                            horaInicio >= horaFinal -> {
                                lay.editClaseNombre.error = null
                                lay.editClaseProfesor.error = null
                                lay.editClaseEdificio.error = null
                                lay.editClaseSalon.error = null
                                lay.editClaseHoraFinal.error = "La duración debe ser mayor a 0"
                            }
                            else -> {
                                lay.editClaseNombre.error = null
                                lay.editClaseProfesor.error = null
                                lay.editClaseEdificio.error = null
                                lay.editClaseSalon.error = null
                                lay.editClaseHoraFinal.error = null

                                val newData = ClaseData(
                                    materia.toUpperCase(Locale.ROOT).replace(" ", "_")+diaIndex.toString()+horaInicio.toString(),
                                    diaIndex,
                                    materia,
                                    horaInicio,
                                    horaFinal,
                                    "#000000",
                                    grupo,
                                    profesor,
                                    edificio,
                                    salon
                                )

                                if (horarioPersonalDatabase.add(newData)) {
                                    refreshMessage()
                                    changeBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)
                                } else {
                                    failedMessage()
                                }

                                if (activity is SAESActivity) {
                                    (activity as SAESActivity).updateWidgets()
                                }

                                editingClase = null
                            }
                        }
                    }
                }
            }
        }

        lay.buttonAddFromHorario.setOnClickListener {
            if (activity != null) {
                AnadirMateriaDialog().show(activity!!.supportFragmentManager, "add_materias_horario_general")
            }
        }
    }

    private fun refreshMessage(){
        if (activity is SAESActivity) {
            Snackbar.make(
                (activity as SAESActivity).getMainLayout(),
                "Refresca tu horario",
                Snackbar.LENGTH_LONG
            ).setAction("Refrescar") {
                if(context is SAESActivity){
                    (context as SAESActivity).postNavigationItemSelected(R.id.nav_horario, false)
                }
            }.show()
        }
    }

    private fun failedMessage(){
        if (activity is SAESActivity) {
            Snackbar.make(
                (activity as SAESActivity).getMainLayout(),
                "No se han podido guardar los cambios",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    fun nextDay(){
        if (!isExpanded) return

        val expand = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            1f
        )
        val collapse = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)

        selectedIndex = if(selectedIndex+1 < 5){
            selectedIndex+1
        }else{
            0
        }

        if (isExpanded) {
            lay.expandedLayout.text = diasName[selectedIndex]

            if (selectedIndex == currentDiaIndex && isCurrentDayVisible) {
                lay.expandedLayout.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorHighlight
                    )
                )
            } else {
                lay.expandedLayout.setTextColor(
                    ContextCompat.getColor(context, R.color.colorPrimaryText)
                )
            }

            lay.expandedLayout.visibility = View.VISIBLE
            lay.diasTextLayout.visibility = View.GONE
        }

        for (listener in onExpandedListeners) {
            listener(isExpanded, selectedIndex)
        }

        for ((index, frame) in diasLayout.withIndex()) {
            if (index != selectedIndex && isExpanded) {
                frame.layoutParams = collapse
            } else {
                frame.layoutParams = expand
            }
        }
    }

    fun prevDay(){
        if (!isExpanded) return

        val expand = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            1f
        )
        val collapse = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)

        selectedIndex = if(selectedIndex-1 >= 0){
            selectedIndex-1
        }else{
            4
        }

        if (isExpanded) {
            lay.expandedLayout.text = diasName[selectedIndex]

            if (selectedIndex == currentDiaIndex && isCurrentDayVisible) {
                lay.expandedLayout.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorHighlight
                    )
                )
            } else {
                lay.expandedLayout.setTextColor(
                    ContextCompat.getColor(context, R.color.colorPrimaryText)
                )
            }

            lay.expandedLayout.visibility = View.VISIBLE
            lay.diasTextLayout.visibility = View.GONE
        }

        for (listener in onExpandedListeners) {
            listener(isExpanded, selectedIndex)
        }

        for ((index, frame) in diasLayout.withIndex()) {
            if (index != selectedIndex && isExpanded) {
                frame.layoutParams = collapse
            } else {
                frame.layoutParams = expand
            }
        }
    }

    fun addOnChangeListener(listener: (isExpanded: Boolean, index: Int) -> Unit) {
        onExpandedListeners.add(listener)
    }

    fun loadData(webViewClient: WebViewClient, url: String, activity: FragmentActivity?, isTransparentBackground : Boolean = false) {
        this.activity = activity

        if(isTransparentBackground){
            lay.horarioParent.setBackgroundColor(Color.TRANSPARENT)
        }

        if (isDatabaseEnabled && !::correccionHorarioDatabase.isInitialized) {
            correccionHorarioDatabase = CorreccionHorarioDatabase(context)
            correccionHorarioDatabase.createTable()
        }

        if (isDatabaseEnabled && !::horarioPersonalDatabase.isInitialized) {
            horarioPersonalDatabase = HorarioPersonalDatabase(context)
        }

        if (context?.isNetworkAvailable() == true) {
            lay.progressBarHorario.visibility = View.VISIBLE

            if (isDatabaseEnabled && !::horarioDatabase.isInitialized) {
                horarioDatabase = HorarioDatabase(context)
                horarioDatabase.deleteTable()
                horarioDatabase.createTable()
            }

            jsi = JSI()

            webView.webViewClient = webViewClient
            if (!jSIadded) {
                webView.addJavascriptInterface(jsi,
                    JsiName
                )
            }

            webView.loadUrl(getUrl(context) + url)
        } else {
            jsi = JSI(true)
            val database = HorarioDatabase(context)
            database.createTable()

            val data = database.getAll()

            if (isDatabaseEnabled && !::horarioDatabase.isInitialized) {
                horarioDatabase = HorarioDatabase(context)
                horarioDatabase.createTable()
            }

            data.moveToPosition(-1)

            while (data.moveToNext()) {
                jsi.addClase(HorarioDatabase.cursorAsClaseData(data))
            }

            jsi.onHorarioFinished()
        }
    }

    fun loadData(clases: Array<ClaseData>, activity: FragmentActivity?, isTransparentBackground : Boolean = false) {
        this.activity = activity

        if(isTransparentBackground){
            lay.horarioParent.setBackgroundColor(Color.TRANSPARENT)
        }

        lay.progressBarHorario.visibility = View.VISIBLE

        jsi = JSI()

        for (data in clases) {
            jsi.addClase(data)
        }

        jsi.onHorarioFinished()
    }

    private fun initFrame(i: Int) {
        val cal = Calendar.getInstance()

        viewTreeObserver.addOnGlobalLayoutListener {
            val hora = cal.get(Calendar.HOUR_OF_DAY) + (cal.get(Calendar.MINUTE) / 60.0)
            if (hora in minHora..maxHora && isCurrentHourVisible) {
                lay.arrowMarker.visibility = View.VISIBLE
                (lay.arrowMarker.layoutParams as LayoutParams).setMargins(
                    0,
                    dpToPixel(context, (((hora - minHora) * claseSize) - 10).toInt()),
                    0,
                    0
                )
            } else {
                lay.arrowMarker.visibility = View.GONE
            }

            if (isCurrentDayVisible) {
                when (currentDiaIndex) {
                    0 -> lay.labelL
                    1 -> lay.labelMa
                    2 -> lay.labelMi
                    3 -> lay.labelJ
                    4 -> lay.labelV
                    else -> null
                }?.setTextColor(ContextCompat.getColor(activity!!, R.color.colorHighlight))
            }
        }

        diasLayout[i].setOnClickListener {
            crashlytics.log("Click en ToggleHorario en la clase ${this.javaClass.canonicalName}")
            toggleDayState(i)
        }
    }

    fun toggleDayState(i: Int) {
        val expand = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            1f
        )
        val collapse = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)

        selectedIndex = i
        isExpanded = !isExpanded

        if (isExpanded) {
            lay.expandedLayout.text = diasName[selectedIndex]

            if (selectedIndex == currentDiaIndex && isCurrentDayVisible) {
                lay.expandedLayout.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorHighlight
                    )
                )
            } else {
                lay.expandedLayout.setTextColor(
                    ContextCompat.getColor(context, R.color.colorPrimaryText)
                )
            }

            lay.expandedLayout.visibility = View.VISIBLE
            lay.diasTextLayout.visibility = View.GONE
        } else {
            lay.expandedLayout.visibility = View.GONE
            lay.diasTextLayout.visibility = View.VISIBLE
        }

        for (listener in onExpandedListeners) {
            listener(isExpanded, selectedIndex)
        }

        for ((index, frame) in diasLayout.withIndex()) {
            if (index != selectedIndex && isExpanded) {
                frame.layoutParams = collapse
            } else {
                frame.layoutParams = expand
            }
        }
    }

    fun clear() {
        for (frame in diasLayout) {
            frame.removeAllViews()
        }

        horasContainer.removeAllViews()
        clasesData.clear()

        minHora = 25.0
        maxHora = -1.0
        claseSize = 70
        hasClass = false
    }

    fun newClase(){
        bottomSheetType = BottomSheetEditType.NEW_CLASS

        if(activity?.isNetworkAvailable() == true){
            lay.newClassCustomLayout.visibility = View.VISIBLE
        }else{
            lay.newClassCustomLayout.visibility = View.GONE
        }

        lay.editClaseNombre.error = null
        lay.editClaseProfesor.error = null
        lay.editClaseEdificio.error = null
        lay.editClaseSalon.error = null

        lay.editClaseNombre.editText?.text = Editable.Factory().newEditable("")
        lay.editClaseProfesor.editText?.text = Editable.Factory().newEditable("")
        lay.editClaseEdificio.editText?.text = Editable.Factory().newEditable("")
        lay.editClaseSalon.editText?.text = Editable.Factory().newEditable("")
        lay.selectableDia.setOptions(arrayOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes"))
        lay.editClaseGrupo?.editText?.isEnabled = true
        lay.editClaseGrupo?.editText?.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryText))
        lay.editClaseGrupo.editText?.text = Editable.Factory().newEditable("")
        lay.selectableDia.setSelection(0)
        lay.editClaseHoraInicio.editText?.text = Editable.Factory().newEditable("12:00")
        lay.editClaseHoraFinal.editText?.text = Editable.Factory().newEditable("13:00")

        lay.editClaseHoraInicio.setEndIconOnClickListener {
            val horaInicio = hourToDouble(lay.editClaseHoraInicio.editText?.text.toString())
            TimePickerDialog(context, R.style.TimePickerTheme, { _, hour, minutes ->
                lay.editClaseHoraInicio.editText?.text = Editable.Factory().newEditable((hour+(minutes/60.0)).toHour())
            }, horaInicio.toInt(), (horaInicio.decimal()*60).toInt(), false).show()
        }

        lay.editClaseHoraFinal.setEndIconOnClickListener {
            val horaFinal = hourToDouble(lay.editClaseHoraFinal.editText?.text.toString())
            TimePickerDialog(context, R.style.TimePickerTheme, { _, hour, minutes ->
                lay.editClaseHoraFinal.editText?.text = Editable.Factory().newEditable((hour+(minutes/60.0)).toHour())
            }, horaFinal.toInt(), (horaFinal.decimal()*60).toInt(), false).show()
        }

        lay.restoreName.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            lay.editClaseNombre.editText?.text =
                Editable.Factory().newEditable("")
        }

        lay.restoreProfesor.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            lay.editClaseProfesor.editText?.text =
                Editable.Factory().newEditable("")
        }

        lay.restoreEdificio.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            lay.editClaseEdificio.editText?.text =
                Editable.Factory().newEditable("")
        }

        lay.restoreSalon.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            lay.editClaseSalon.editText?.text =
                Editable.Factory().newEditable("")
        }

        lay.restoreDia.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            lay.selectableDia.setSelection(0)
        }

        lay.restoreHoraInicio.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            lay.editClaseHoraInicio.editText?.text =
                Editable.Factory().newEditable("12:00")
        }

        lay.restoreFinal.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            lay.editClaseHoraFinal.editText?.text =
                Editable.Factory().newEditable("13:00")

            lay.editClaseHoraFinal.error = null
        }

        changeBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
    }

    fun newClase(claseData: HorarioGeneradoDatabase.Data){
        val generadorToClaseData = fun (dia: Int) : ClaseData?{
            val horas = dividirHoras(when(dia){
                0 -> claseData.lunes
                1 -> claseData.martes
                2 -> claseData.miercoles
                3 -> claseData.jueves
                4 -> claseData.viernes
                else -> ""
            }) ?: return null
            return ClaseData(
                claseData.grupo+claseData.materia.toUpperCase(Locale.ROOT).replace(" ", "_")+dia.toString()+horas.first,
                dia,
                claseData.materia,
                horas.first,
                horas.second,
                "#000000",
                claseData.grupo,
                claseData.profesor,
                claseData.edificio,
                claseData.salon,
                true
            )
        }

        var success = false

        for(i in 0..4){
            val data = generadorToClaseData(i) ?: continue
            if (horarioPersonalDatabase.add(data)) {
                success = true
            }
        }

        if(success){
            refreshMessage()
            changeBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)
        }else{
            failedMessage()
        }

        if (activity is SAESActivity) {
            (activity as SAESActivity).updateWidgets()
        }

        changeBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)
    }

    fun editClase(clase: ClaseData) {
        lay.newClassCustomLayout.visibility = View.GONE
        bottomSheetType = BottomSheetEditType.EDIT_CLASS
        lay.editClaseNombre.error = null
        lay.editClaseProfesor.error = null
        lay.editClaseEdificio.error = null
        lay.editClaseSalon.error = null
        editingClase = clase
        lay.editClaseNombre.editText?.text = Editable.Factory().newEditable(clase.materia.toProperCase())
        lay.editClaseProfesor.editText?.text = Editable.Factory().newEditable(clase.profesor.toProperCase())
        lay.editClaseEdificio.editText?.text = Editable.Factory().newEditable(clase.edificio)
        lay.editClaseSalon.editText?.text = Editable.Factory().newEditable(clase.salon)
        lay.editClaseGrupo.editText?.text = Editable.Factory().newEditable(clase.grupo)
        lay.selectableDia.setOptions(arrayOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes"))
        lay.editClaseGrupo?.editText?.isEnabled = false
        lay.editClaseGrupo?.editText?.setTextColor(ContextCompat.getColor(context, R.color.colorDivider))
        lay.selectableDia.setSelection(clase.diaIndex)
        lay.editClaseHoraInicio.editText?.text = Editable.Factory().newEditable(clase.horaInicio.toHour())
        lay.editClaseHoraFinal.editText?.text = Editable.Factory().newEditable(clase.horaFinal.toHour())

        lay.editClaseHoraInicio.setEndIconOnClickListener {
            TimePickerDialog(context, R.style.TimePickerTheme, { _, hour, minutes ->
                lay.editClaseHoraInicio.editText?.text = Editable.Factory().newEditable((hour+(minutes/60.0)).toHour())
            }, clase.horaInicio.toInt(), (clase.horaInicio.decimal()*60).toInt(), false).show()
        }

        lay.editClaseHoraFinal.setEndIconOnClickListener {
            TimePickerDialog(context, R.style.TimePickerTheme, { _, hour, minutes ->
                lay.editClaseHoraFinal.editText?.text = Editable.Factory().newEditable((hour+(minutes/60.0)).toHour())
            }, clase.horaFinal.toInt(), (clase.horaFinal.decimal()*60).toInt(), false).show()
        }

        if (::horarioDatabase.isInitialized) {
            val restoreData = horarioDatabase.searchData(clase)

            if (restoreData != null) {
                lay.restoreName.setOnClickListener {
                    crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                    lay.editClaseNombre.editText?.text =
                        Editable.Factory().newEditable(restoreData.materia.toProperCase())
                }

                lay.restoreProfesor.setOnClickListener {
                    crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                    lay.editClaseProfesor.editText?.text =
                        Editable.Factory().newEditable(restoreData.profesor.toProperCase())
                }

                lay.restoreEdificio.setOnClickListener {
                    crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                    lay.editClaseEdificio.editText?.text =
                        Editable.Factory().newEditable(restoreData.edificio)
                }

                lay.restoreSalon.setOnClickListener {
                    crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                    lay.editClaseSalon.editText?.text =
                        Editable.Factory().newEditable(restoreData.salon)
                }

                lay.restoreDia.setOnClickListener {
                    crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                    lay.selectableDia.setSelection(restoreData.diaIndex)
                }

                lay.restoreHoraInicio.setOnClickListener {
                    crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                    lay.editClaseHoraInicio.editText?.text =
                        Editable.Factory().newEditable(restoreData.horaInicio.toHour())
                }

                lay.restoreFinal.setOnClickListener {
                    crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                    lay.editClaseHoraFinal.editText?.text =
                        Editable.Factory().newEditable(restoreData.horaFinal.toHour())
                }
            }
        }

        changeBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
    }

    fun removeUserCustomClase(claseData: ClaseData){
        if(isDatabaseEnabled && ::horarioPersonalDatabase.isInitialized){
            if(horarioPersonalDatabase.deleteMateriaById(claseData.id)){
                refreshMessage()
                if(activity is SAESActivity){
                    (activity as SAESActivity).updateWidgets()
                }
            }else{
                failedMessage()
            }
        }
    }

    inner class JSI(private val isOffline: Boolean = false) {
        private val materiasKey = ArrayList<String>()
        private val materiasColor = ArrayList<String>()
        private var colorIndex = 0
        private val colores = context.resources.getStringArray(R.array.paletaHorario)
        private var canSnackbar = true

        @JavascriptInterface
        fun addClase(
            id: String, diaIndex: Int, materia: String, hora: String,
            grupo: String, profesor: String, edificio: String, salon: String
        ) {
            if(id.isBlank())return

            val color = if (materiasKey.contains(materia)) {
                materiasColor[materiasKey.indexOf(materia)]
            } else {
                canSnackbar = true
                materiasKey.add(materia)
                materiasColor.add(colores[colorIndex++])
                materiasColor.last()
            }

            val horaDouble = dividirHoras(hora)

            if (horaDouble != null) {
                val clase = ClaseData(
                    id.replace(" ", "_")+horaDouble.first.toString(),
                    diaIndex,
                    materia,
                    horaDouble.first,
                    horaDouble.second,
                    color,
                    grupo,
                    profesor,
                    edificio,
                    salon
                )

                if (::horarioDatabase.isInitialized && !isOffline) {
                    horarioDatabase.add(clase)
                }

                if (::correccionHorarioDatabase.isInitialized) {
                    val correccion = correccionHorarioDatabase.searchData(clase)
                    if (correccion != null) {
                        clase.materia = correccion.materia
                        clase.grupo = correccion.grupo
                        clase.profesor = correccion.profesor
                        clase.edificio = correccion.edificio
                        clase.salon = correccion.salon
                        clase.diaIndex = correccion.diaIndex
                        clase.horaInicio = correccion.horaInicio
                        clase.horaFinal = correccion.horaFinal
                    }
                }

                if (clase.horaInicio < minHora) {
                    minHora = clase.horaInicio
                }

                if (clase.horaFinal > maxHora) {
                    maxHora = clase.horaFinal
                }

                if (clase.horaFinal - clase.horaInicio <= 1.0) {
                    claseSize = 100
                }

                hasClass = true

                clasesData.add(clase)
            }
        }

        fun addClase(claseData: ClaseData) {
            if(claseData.id.trim().isEmpty())return

            val color = if (materiasKey.contains(claseData.materia)) {
                materiasColor[materiasKey.indexOf(claseData.materia)]
            } else {
                materiasKey.add(claseData.materia)
                materiasColor.add(colores[colorIndex++])
                materiasColor.last()
            }

            claseData.color = color

            if (::horarioDatabase.isInitialized && !isOffline) {
                horarioDatabase.add(claseData)
            }

            if (::correccionHorarioDatabase.isInitialized) {
                val correccion = correccionHorarioDatabase.searchData(claseData)
                if (correccion != null) {
                    claseData.materia = correccion.materia
                    claseData.grupo = correccion.grupo
                    claseData.profesor = correccion.profesor
                    claseData.edificio = correccion.edificio
                    claseData.salon = correccion.salon
                    claseData.diaIndex = correccion.diaIndex
                    claseData.horaInicio = correccion.horaInicio
                    claseData.horaFinal = correccion.horaFinal
                }
            }

            if (claseData.horaInicio < minHora) {
                minHora = claseData.horaInicio
            }

            if (claseData.horaFinal > maxHora) {
                maxHora = claseData.horaFinal
            }

            if (claseData.horaFinal - claseData.horaInicio <= 1.0) {
                claseSize = 100
            }

            hasClass = true

            clasesData.add(claseData)
        }

        @SuppressLint("SetTextI18n")
        @JavascriptInterface
        fun onHorarioFinished() {
            if(isDatabaseEnabled && ::horarioPersonalDatabase.isInitialized){
                val all = horarioPersonalDatabase.getAll()

                while (all.moveToNext()){
                    addClase(HorarioPersonalDatabase.cursorAsClaseData(all))
                }

                all.close()
            }

            if(!hasClass){
                if (activity is SAESActivity) {
                    (activity as SAESActivity).showEmptyText("No se han encontrado clases")
                }
            }else{
                if (activity is SAESActivity) {
                    (activity as SAESActivity).hideEmptyText()
                }
            }



            for(clase in clasesData){
                activity?.runOnUiThread {
                    diasLayout[clase.diaIndex].addView(
                        ClaseView(
                            activity,
                            clase,
                            this@HorarioView
                        )
                    )
                }
            }

            materiasKey.clear()
            materiasColor.clear()
            colorIndex = 0

            if (activity is SAESActivity && isDatabaseEnabled) {
                (activity as SAESActivity).updateWidgets()
            }

            val horaSize =
                LinearLayout.LayoutParams(dpToPixel(context, 45), dpToPixel(context, claseSize - 1))

            val dividerParams =
                LinearLayout.LayoutParams(dpToPixel(context, 45), dpToPixel(context, 1))
            val dividerColor = ContextCompat.getColor(context, R.color.colorDivider)
            val textColor = ContextCompat.getColor(context, R.color.colorPrimaryText)

            minHora = minHora.toInt().toDouble()
            maxHora = maxHora.toInt().toDouble() + offsetBottom

            activity?.runOnUiThread {
                for (i in minHora.toInt()..maxHora.toInt()) {
                    val textView = TextView(context)
                    textView.text = "$i:00"
                    textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    textView.layoutParams = horaSize
                    textView.setTextColor(textColor)

                    val divider = View(context)
                    divider.layoutParams = dividerParams
                    divider.setBackgroundColor(dividerColor)

                    horasContainer.addView(divider)
                    horasContainer.addView(textView)
                }

                lay.progressBarHorario.visibility = View.GONE
            }

            clasesData.clear()
        }
    }

    fun closeDatabases() {
        if(::horarioDatabase.isInitialized){
            horarioDatabase.close()
        }

        if(::correccionHorarioDatabase.isInitialized){
            correccionHorarioDatabase.close()
        }
    }
}