package ziox.ramiro.saes.views.schedule_view

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.os.Looper
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
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.databases.*
import ziox.ramiro.saes.databinding.ViewClassScheduleBinding
import ziox.ramiro.saes.dialogs.AddCourseDialogFragment
import ziox.ramiro.saes.fragments.ClassScheduleFragment
import ziox.ramiro.saes.utils.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Creado por Ramiro el 11/11/2018 a las 4:29 PM para SAESv2.
 */

const val TICK_UPDATE_MILLIS = 5000L

class ScheduleView : FrameLayout {
    private val crashlytics = FirebaseCrashlytics.getInstance()
    companion object {
        const val JSI_NAME = "JSI"
    }

    private enum class BottomSheetEditType{
        EDIT_CLASS, NEW_CLASS
    }

    private lateinit var bottomSheet: BottomSheetBehavior<LinearLayout>
    lateinit var mainView: ViewClassScheduleBinding

    private lateinit var originalClassScheduleDao: OriginalClassScheduleDao
    private lateinit var adjustedClassScheduleDao: AdjustedClassScheduleDao
    private lateinit var personalClassScheduleDao: PersonalClassScheduleDao
    private val dayLayouts = ArrayList<FrameLayout>()
    lateinit var jsi: JSI
        private set

    private var dayNames: ArrayList<String> = arrayListOf(
        "Lunes",
        "Martes",
        "Miercoles",
        "Jueves",
        "Viernes"
    )
    private val onExpandedListeners = ArrayList<(isExpanded: Boolean, index: Int) -> Unit>()
    private val classesData = ArrayList<ScheduleClass>()
    private var tickHandler : Handler? = Handler(Looper.getMainLooper())
    val webView: WebView

    var minHour = 25.0
    var maxHour = -1.0
    var classSize = 70
    var offsetBottom = 0
    var canEdit = false
    var isDatabaseEnabled = false
    var isCurrentDayVisible = false
    private var isCurrentDayOpenOnStartEnable = false
    private var bottomSheetType = BottomSheetEditType.EDIT_CLASS
    var isExpanded = false
    var isCurrentHourVisible = false
    var selectedIndex = 0
    var isAddInsetTopEnabled = false
        set(value) {
            if(::mainView.isInitialized){
                if(value){
                    mainView.scheduleSection.addTopInsetPadding()
                    mainView.bottomSheetAppBarLayout.addTopInsetPadding()
                }else{
                    mainView.scheduleSection.updatePadding(top = 0)
                    mainView.bottomSheetAppBarLayout.updatePadding(top = 0)
                }
            }
            field = value
        }
    private var hasClass = false

    private var activity: FragmentActivity? = null
    private var isJSIAdded = false
    private var currentEditingClass: ScheduleClass? = null
    private var currentDayIndex = -1

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
            mainView.editClassBottomSheet.visibility = View.VISIBLE
            Handler().postDelayed({
                bottomSheet.state = state
            }, 50)
        }
    }

    private fun initLayout() {
        mainView = ViewClassScheduleBinding.inflate(LayoutInflater.from(context), this, true)
        mainView.bottomSheetScrollView.addBottomInsetPadding()
        bottomSheet = BottomSheetBehavior.from(mainView.editClassBottomSheet)

        bottomSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}

            override fun onStateChanged(p0: View, p1: Int) {
                if (p1 == BottomSheetBehavior.STATE_HIDDEN) {
                    mainView.editClassBottomSheet.visibility = View.GONE
                    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(this@ScheduleView.windowToken, 0)
                    if(activity is SAESActivity){
                        (activity as SAESActivity).showFab()
                    }
                    if(activity?.supportFragmentManager?.fragments?.first() is ClassScheduleFragment){
                        (activity?.supportFragmentManager?.fragments?.first() as ClassScheduleFragment).setPaginationVisible(true)
                    }
                }else if(p1 == BottomSheetBehavior.STATE_EXPANDED){
                    if(activity is SAESActivity){
                        (activity as SAESActivity).hideFab()
                        (activity?.supportFragmentManager?.fragments?.first() as ClassScheduleFragment).setPaginationVisible(false)
                    }
                }
            }
        })

        currentDayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2

        changeBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)

        val tran = LayoutTransition()
        tran.enableTransitionType(LayoutTransition.CHANGING)
        tran.setDuration(300)
        mainView.daysContainer.layoutTransition = tran

        for (i in 0 until mainView.daysContainer.childCount) {
            val dia = mainView.daysContainer.getChildAt(i) as FrameLayout
            dayLayouts.add(dia)
            initFrame(i)
        }

        if (isCurrentDayOpenOnStartEnable) {
            if (currentDayIndex in 0..4) {
                toggleDayState(currentDayIndex)
            }
        }

        mainView.closeEditBottomSheetButton.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            currentEditingClass = null
            changeBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)
        }

        mainView.saveEditClassButton.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            when(bottomSheetType){
                BottomSheetEditType.EDIT_CLASS -> {
                    if (currentEditingClass != null && ::adjustedClassScheduleDao.isInitialized && ::originalClassScheduleDao.isInitialized) {
                        val newData = currentEditingClass!!.copy()

                        try {
                            newData.courseName = mainView.courseNameEditInput.editText!!.text.toString()
                            newData.teacherName = mainView.teacherNameEditInput.editText!!.text.toString()
                            newData.buildingName = mainView.buildingNameEditInput.editText!!.text.toString()
                            newData.classroomName = mainView.classroomNameEditInput.editText!!.text.toString()
                            newData.dayIndex = mainView.dayEditSelectable.selectedIndex
                            newData.startHour = hourToDouble(mainView.startHourEditInput.editText!!.text.toString())
                            newData.finishHour = hourToDouble(mainView.finishHourEditInput.editText!!.text.toString())
                        } catch (e: Exception) {
                            Log.e(this.javaClass.canonicalName, e.toString())
                        }

                        adjustedClassScheduleDao.delete(newData.toAdjustedScheduleClass())
                        adjustedClassScheduleDao.insert(newData.toAdjustedScheduleClass())

                        refreshMessage()
                        changeBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)

                        if (activity is SAESActivity) {
                            (activity as SAESActivity).updateWidgets()
                        }

                        currentEditingClass = null
                    }
                }
                BottomSheetEditType.NEW_CLASS -> {
                    if (::personalClassScheduleDao.isInitialized && ::originalClassScheduleDao.isInitialized) {
                        val courseName = mainView.courseNameEditInput.editText!!.text.toString()
                        val teacherName = mainView.teacherNameEditInput.editText!!.text.toString()
                        val buildingName = mainView.buildingNameEditInput.editText!!.text.toString()
                        val classroomName = mainView.classroomNameEditInput.editText!!.text.toString()
                        val group = mainView.editClaseGrupo.editText!!.text.toString()
                        val dayIndex = mainView.dayEditSelectable.selectedIndex
                        val startHour = hourToDouble(mainView.startHourEditInput.editText!!.text.toString())
                        val finishHour = hourToDouble(mainView.finishHourEditInput.editText!!.text.toString())

                        when{
                            courseName.isBlank() -> mainView.courseNameEditInput.error = "Este campo está vacío"
                            teacherName.isBlank() -> {
                                mainView.courseNameEditInput.error = null
                                mainView.teacherNameEditInput.error = "Este campo está vacío"
                            }
                            buildingName.isBlank() -> {
                                mainView.courseNameEditInput.error = null
                                mainView.teacherNameEditInput.error = null
                                mainView.buildingNameEditInput.error = "Este campo está vacío"
                            }
                            classroomName.isBlank() -> {
                                mainView.courseNameEditInput.error = null
                                mainView.teacherNameEditInput.error = null
                                mainView.buildingNameEditInput.error = null
                                mainView.classroomNameEditInput.error = "Este campo está vacío"
                            }
                            startHour >= finishHour -> {
                                mainView.courseNameEditInput.error = null
                                mainView.teacherNameEditInput.error = null
                                mainView.buildingNameEditInput.error = null
                                mainView.classroomNameEditInput.error = null
                                mainView.finishHourEditInput.error = "La duración debe ser mayor a 0"
                            }
                            else -> {
                                mainView.courseNameEditInput.error = null
                                mainView.teacherNameEditInput.error = null
                                mainView.buildingNameEditInput.error = null
                                mainView.classroomNameEditInput.error = null
                                mainView.finishHourEditInput.error = null

                                val newData = ScheduleClass(
                                    courseName.toUpperCase(Locale.ROOT).replace(" ", "_")+dayIndex.toString()+startHour.toString(),
                                    dayIndex,
                                    courseName,
                                    startHour,
                                    finishHour,
                                    "#000000",
                                    group,
                                    teacherName,
                                    buildingName,
                                    classroomName,
                                    true
                                )

                                personalClassScheduleDao.insert(newData.toPersonalScheduleClass())
                                refreshMessage()
                                changeBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)

                                if (activity is SAESActivity) {
                                    (activity as SAESActivity).updateWidgets()
                                }

                                currentEditingClass = null
                            }
                        }
                    }
                }
            }
        }

        mainView.buttonAddFromHorario.setOnClickListener {
            if (activity != null) {
                AddCourseDialogFragment().show(activity!!.supportFragmentManager, "add_materias_horario_general")
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
            mainView.expandedLayout.text = dayNames[selectedIndex]

            if (selectedIndex == currentDayIndex && isCurrentDayVisible) {
                mainView.expandedLayout.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorDanger
                    )
                )
            } else {
                mainView.expandedLayout.setTextColor(
                    ContextCompat.getColor(context, R.color.colorTextPrimary)
                )
            }

            mainView.expandedLayout.visibility = View.VISIBLE
            mainView.diasTextLayout.visibility = View.GONE
        }

        for (listener in onExpandedListeners) {
            listener(isExpanded, selectedIndex)
        }

        for ((index, frame) in dayLayouts.withIndex()) {
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
            mainView.expandedLayout.text = dayNames[selectedIndex]

            if (selectedIndex == currentDayIndex && isCurrentDayVisible) {
                mainView.expandedLayout.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorDanger
                    )
                )
            } else {
                mainView.expandedLayout.setTextColor(
                    ContextCompat.getColor(context, R.color.colorTextPrimary)
                )
            }

            mainView.expandedLayout.visibility = View.VISIBLE
            mainView.diasTextLayout.visibility = View.GONE
        }

        for (listener in onExpandedListeners) {
            listener(isExpanded, selectedIndex)
        }

        for ((index, frame) in dayLayouts.withIndex()) {
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
            mainView.parent.setBackgroundColor(Color.TRANSPARENT)
        }

        if (isDatabaseEnabled && !::adjustedClassScheduleDao.isInitialized) {
            adjustedClassScheduleDao = AppLocalDatabase.getInstance(context).adjustedClassScheduleDao()
        }

        if (isDatabaseEnabled && !::personalClassScheduleDao.isInitialized) {
            personalClassScheduleDao = AppLocalDatabase.getInstance(context).personalClassScheduleDao()
        }

        if (context?.isNetworkAvailable() == true) {
            mainView.scheduleProgressBar.visibility = View.VISIBLE

            if (isDatabaseEnabled && !::originalClassScheduleDao.isInitialized) {
                originalClassScheduleDao = AppLocalDatabase.getInstance(context).originalClassScheduleDao()
                originalClassScheduleDao.deleteAll()
            }

            jsi = JSI()

            webView.webViewClient = webViewClient
            if (!isJSIAdded) {
                webView.addJavascriptInterface(jsi,
                    JSI_NAME
                )
            }

            webView.loadUrl(getUrl(context) + url)
        } else {
            jsi = JSI(true)
            val database = AppLocalDatabase.getInstance(context).originalClassScheduleDao()

            val data = database.getAll()

            if (isDatabaseEnabled && !::originalClassScheduleDao.isInitialized) {
                originalClassScheduleDao = database
            }

            for (scheduleClass in data){
                jsi.addClass(scheduleClass)
            }

            jsi.onScheduleFinished()
        }
    }

    fun loadData(classes: Array<OriginalScheduleClass>, activity: FragmentActivity?, isTransparentBackground : Boolean = false) {
        this.activity = activity

        if(isTransparentBackground){
            mainView.parent.setBackgroundColor(Color.TRANSPARENT)
        }

        mainView.scheduleProgressBar.visibility = View.VISIBLE

        jsi = JSI()

        for (data in classes) {
            jsi.addClass(data)
        }

        jsi.onScheduleFinished()
    }

    private fun calculateCurrentHourPosition(){
        val cal = Calendar.getInstance()

        val hora = cal.get(Calendar.HOUR_OF_DAY) + (cal.get(Calendar.MINUTE) / 60.0) + (cal.get(Calendar.SECOND) / 3600.0)
        if (hora in minHour..maxHour && isCurrentHourVisible) {
            mainView.arrowMarker.visibility = View.VISIBLE
            Log.d(this.javaClass.canonicalName,"$hora ${dpToPixel(context, (((hora - minHour) * classSize) - 10).toInt())}")
            (mainView.arrowMarker.layoutParams as LayoutParams).setMargins(
                0,
                dpToPixel(context, (((hora - minHour) * classSize) - 10).toInt()),
                0,
                0
            )
        } else {
            mainView.arrowMarker.visibility = View.GONE
        }

        if (isCurrentDayVisible) {
            when (currentDayIndex) {
                0 -> mainView.labelL
                1 -> mainView.labelMa
                2 -> mainView.labelMi
                3 -> mainView.labelJ
                4 -> mainView.labelV
                else -> null
            }?.setTextColor(ContextCompat.getColor(context, R.color.colorDanger))
        }
    }

    private fun initFrame(i: Int) {
        calculateCurrentHourPosition()
        tickHandler?.postDelayed(object : Runnable{
            override fun run() {
                calculateCurrentHourPosition()

                tickHandler?.postDelayed(this, TICK_UPDATE_MILLIS)
            }
        }, TICK_UPDATE_MILLIS)

        dayLayouts[i].setOnClickListener {
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
            mainView.expandedLayout.text = dayNames[selectedIndex]

            if (selectedIndex == currentDayIndex && isCurrentDayVisible) {
                mainView.expandedLayout.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorDanger
                    )
                )
            } else {
                mainView.expandedLayout.setTextColor(
                    ContextCompat.getColor(context, R.color.colorTextPrimary)
                )
            }

            mainView.expandedLayout.visibility = View.VISIBLE
            mainView.diasTextLayout.visibility = View.GONE
        } else {
            mainView.expandedLayout.visibility = View.GONE
            mainView.diasTextLayout.visibility = View.VISIBLE
        }

        for (listener in onExpandedListeners) {
            listener(isExpanded, selectedIndex)
        }

        for ((index, frame) in dayLayouts.withIndex()) {
            if (index != selectedIndex && isExpanded) {
                frame.layoutParams = collapse
            } else {
                frame.layoutParams = expand
            }
        }
    }

    fun clear() {
        for (frame in dayLayouts) {
            frame.removeAllViews()
        }

        mainView.hoursContainer.removeAllViews()
        classesData.clear()

        minHour = 25.0
        maxHour = -1.0
        classSize = 70
        hasClass = false
    }

    private fun setBottomSheetInputEndIcon(@DrawableRes resId : Int){
        val drawable = ResourcesCompat.getDrawable(resources, resId, context.theme)
        drawable?.setTint(ContextCompat.getColor(context, R.color.colorSecondary))

        mainView.courseNameEditInput.endIconDrawable = drawable
        mainView.teacherNameEditInput.endIconDrawable = drawable
        mainView.buildingNameEditInput.endIconDrawable = drawable
        mainView.classroomNameEditInput.endIconDrawable = drawable
        mainView.restoreDia.setImageDrawable(drawable)
        mainView.editClaseGrupo.endIconDrawable = drawable
        mainView.startHourEditInput.endIconDrawable = drawable
        mainView.finishHourEditInput.endIconDrawable = drawable
    }

    fun newClass(){
        bottomSheetType = BottomSheetEditType.NEW_CLASS
        mainView.bottomSheetTitle.text = "Nueva clase"

        if(activity?.isNetworkAvailable() == true){
            mainView.newClassCustomLayout.visibility = View.VISIBLE
        }else{
            mainView.newClassCustomLayout.visibility = View.GONE
        }

        mainView.courseNameEditInput.error = null
        mainView.teacherNameEditInput.error = null
        mainView.buildingNameEditInput.error = null
        mainView.classroomNameEditInput.error = null

        mainView.courseNameEditInput.editText?.text = Editable.Factory().newEditable("")
        mainView.teacherNameEditInput.editText?.text = Editable.Factory().newEditable("")
        mainView.buildingNameEditInput.editText?.text = Editable.Factory().newEditable("")
        mainView.classroomNameEditInput.editText?.text = Editable.Factory().newEditable("")
        mainView.dayEditSelectable.setOptions(arrayOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes"))
        mainView.editClaseGrupo.editText?.isEnabled = true
        mainView.editClaseGrupo.editText?.setTextColor(ContextCompat.getColor(context, R.color.colorTextPrimary))
        mainView.editClaseGrupo.editText?.text = Editable.Factory().newEditable("")
        mainView.dayEditSelectable.setSelection(0)
        mainView.startHourEditInput.editText?.text = Editable.Factory().newEditable("12:00")
        mainView.finishHourEditInput.editText?.text = Editable.Factory().newEditable("13:00")

        setBottomSheetInputEndIcon(R.drawable.ic_baseline_cancel_24)

        mainView.startHourEditInput.editText?.setOnFocusChangeListener { view, isInFocus ->
            if(isInFocus){
                val horaInicio = hourToDouble(mainView.startHourEditInput.editText?.text.toString())
                TimePickerDialog(context, R.style.TimePickerTheme, { _, hour, minutes ->
                    mainView.startHourEditInput.editText?.text = Editable.Factory().newEditable((hour+(minutes/60.0)).toHour())
                }, horaInicio.toInt(), (horaInicio.decimal()*60).toInt(), false).show()
            }
            view.clearFocus()
        }

        mainView.finishHourEditInput.editText?.setOnFocusChangeListener { view, isInFocus ->
            if (isInFocus) {
                val horaFinal = hourToDouble(mainView.finishHourEditInput.editText?.text.toString())
                TimePickerDialog(context, R.style.TimePickerTheme, { _, hour, minutes ->
                    mainView.finishHourEditInput.editText?.text = Editable.Factory().newEditable((hour+(minutes/60.0)).toHour())
                }, horaFinal.toInt(), (horaFinal.decimal()*60).toInt(), false).show()
            }
            view.clearFocus()
        }

        mainView.courseNameEditInput.setEndIconOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            mainView.courseNameEditInput.editText?.text = Editable.Factory().newEditable("")
        }

        mainView.teacherNameEditInput.setEndIconOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            mainView.teacherNameEditInput.editText?.text =
                Editable.Factory().newEditable("")
        }

        mainView.buildingNameEditInput.setEndIconOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            mainView.buildingNameEditInput.editText?.text =
                Editable.Factory().newEditable("")
        }

        mainView.classroomNameEditInput.setEndIconOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            mainView.classroomNameEditInput.editText?.text =
                Editable.Factory().newEditable("")
        }

        mainView.restoreDia.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            mainView.dayEditSelectable.setSelection(0)
        }

        mainView.startHourEditInput.setEndIconOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            mainView.startHourEditInput.editText?.text =
                Editable.Factory().newEditable("12:00")
        }

        mainView.finishHourEditInput.setEndIconOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            mainView.finishHourEditInput.editText?.text =
                Editable.Factory().newEditable("13:00")

            mainView.finishHourEditInput.error = null
        }

        changeBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
    }

    fun newClass(scheduleGeneratorClass: ScheduleGeneratorClass){
        val generatorDataToScheduleClass = fun (dia: Int) : ScheduleClass?{
            val horas = dividirHoras(when(dia){
                0 -> scheduleGeneratorClass.monday
                1 -> scheduleGeneratorClass.tuesday
                2 -> scheduleGeneratorClass.wednesday
                3 -> scheduleGeneratorClass.thursday
                4 -> scheduleGeneratorClass.friday
                else -> ""
            }) ?: return null

            return ScheduleClass(
                scheduleGeneratorClass.group+scheduleGeneratorClass.courseName.toUpperCase(Locale.ROOT).replace(" ", "_")+dia.toString()+horas.first,
                dia,
                scheduleGeneratorClass.courseName,
                horas.first,
                horas.second,
                "#000000",
                scheduleGeneratorClass.group,
                scheduleGeneratorClass.teacherName,
                scheduleGeneratorClass.buildingName,
                scheduleGeneratorClass.classroomName,
                true
            )
        }

        var success = false

        for(i in 0..4){
            val data = generatorDataToScheduleClass(i) ?: continue
            personalClassScheduleDao.insert(data.toPersonalScheduleClass())
            success = true
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

    fun editClass(scheduleClass: ScheduleClass) {
        mainView.bottomSheetTitle.text = "Editar clase"

        mainView.newClassCustomLayout.visibility = View.GONE
        bottomSheetType = BottomSheetEditType.EDIT_CLASS
        mainView.courseNameEditInput.error = null
        mainView.teacherNameEditInput.error = null
        mainView.buildingNameEditInput.error = null
        mainView.classroomNameEditInput.error = null
        currentEditingClass = scheduleClass
        mainView.courseNameEditInput.editText?.text = Editable.Factory().newEditable(scheduleClass.courseName.toProperCase())
        mainView.teacherNameEditInput.editText?.text = Editable.Factory().newEditable(scheduleClass.teacherName.toProperCase())
        mainView.buildingNameEditInput.editText?.text = Editable.Factory().newEditable(scheduleClass.buildingName)
        mainView.classroomNameEditInput.editText?.text = Editable.Factory().newEditable(scheduleClass.classroomName)
        mainView.editClaseGrupo.editText?.text = Editable.Factory().newEditable(scheduleClass.group)
        mainView.dayEditSelectable.setOptions(arrayOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes"))
        mainView.editClaseGrupo.editText?.isEnabled = false
        mainView.editClaseGrupo.editText?.setTextColor(ContextCompat.getColor(context, R.color.colorDivider))
        mainView.dayEditSelectable.setSelection(scheduleClass.dayIndex)
        mainView.startHourEditInput.editText?.text = Editable.Factory().newEditable(scheduleClass.startHour.toHour())
        mainView.finishHourEditInput.editText?.text = Editable.Factory().newEditable(scheduleClass.finishHour.toHour())

        setBottomSheetInputEndIcon(R.drawable.ic_baseline_restore_24)

        mainView.startHourEditInput.editText?.setOnFocusChangeListener { view, isInFocus ->
            if(isInFocus){
                TimePickerDialog(context, R.style.TimePickerTheme, { _, hour, minutes ->
                    mainView.startHourEditInput.editText?.text = Editable.Factory().newEditable((hour+(minutes/60.0)).toHour())
                }, scheduleClass.startHour.toInt(), (scheduleClass.startHour.decimal()*60).toInt(), false).show()
            }
            view.clearFocus()
        }

        mainView.finishHourEditInput.editText?.setOnFocusChangeListener { view, isInFocus ->
            if(isInFocus){
                TimePickerDialog(context, R.style.TimePickerTheme, { _, hour, minutes ->
                    mainView.finishHourEditInput.editText?.text = Editable.Factory().newEditable((hour+(minutes/60.0)).toHour())
                }, scheduleClass.finishHour.toInt(), (scheduleClass.finishHour.decimal()*60).toInt(), false).show()
            }
            view.clearFocus()
        }

        if (::originalClassScheduleDao.isInitialized) {
            val restoreData = originalClassScheduleDao.get(scheduleClass.uid)

            if (restoreData != null) {
                mainView.courseNameEditInput.setEndIconOnClickListener {
                    crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                    mainView.courseNameEditInput.editText?.text =
                        Editable.Factory().newEditable(restoreData.courseName.toProperCase())
                }

                mainView.teacherNameEditInput.setEndIconOnClickListener {
                    crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                    mainView.teacherNameEditInput.editText?.text =
                        Editable.Factory().newEditable(restoreData.teacherName.toProperCase())
                }

                mainView.buildingNameEditInput.setEndIconOnClickListener {
                    crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                    mainView.buildingNameEditInput.editText?.text =
                        Editable.Factory().newEditable(restoreData.buildingName)
                }

                mainView.classroomNameEditInput.setEndIconOnClickListener {
                    crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                    mainView.classroomNameEditInput.editText?.text =
                        Editable.Factory().newEditable(restoreData.classroomName)
                }

                mainView.restoreDia.setOnClickListener {
                    crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                    mainView.dayEditSelectable.setSelection(restoreData.dayIndex)
                }

                mainView.startHourEditInput.setEndIconOnClickListener {
                    crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                    mainView.startHourEditInput.editText?.text =
                        Editable.Factory().newEditable(restoreData.startHour.toHour())
                }

                mainView.finishHourEditInput.setEndIconOnClickListener {
                    crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                    mainView.finishHourEditInput.editText?.text =
                        Editable.Factory().newEditable(restoreData.finishHour.toHour())
                }
            }
        }

        changeBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
    }

    fun removeUserCustomClass(scheduleClass: ScheduleClass){
        if(isDatabaseEnabled && ::personalClassScheduleDao.isInitialized){
            personalClassScheduleDao.delete(scheduleClass.toPersonalScheduleClass())
            refreshMessage()
            if(activity is SAESActivity){
                (activity as SAESActivity).updateWidgets()
            }
        }
    }

    inner class JSI(private val isOffline: Boolean = false) {
        private val courseUsedKeys = ArrayList<String>()
        private val courseUsedColors = ArrayList<String>()
        private var colorIndex = 0
        private val resourceColors = context.resources.getStringArray(R.array.paletaHorario)
        private var canDisplaySnackBar = true

        @JavascriptInterface
        fun addClass(
            uid: String, dayIndex: Int, courseName: String, hours: String,
            group: String, teacherName: String, buildingName: String, classroomName: String
        ) {
            if(uid.isBlank())return

            val color = if (courseUsedKeys.contains(courseName)) {
                courseUsedColors[courseUsedKeys.indexOf(courseName)]
            } else {
                canDisplaySnackBar = true
                courseUsedKeys.add(courseName)
                courseUsedColors.add(resourceColors[colorIndex++])
                courseUsedColors.last()
            }

            val horaDouble = dividirHoras(hours)

            if (horaDouble != null) {
                val originalScheduleClass = OriginalScheduleClass(
                    "${uid.replace(" ", "_")}_${horaDouble.first}_${horaDouble.second}",
                    dayIndex,
                    courseName,
                    horaDouble.first,
                    horaDouble.second,
                    color,
                    group,
                    teacherName,
                    buildingName,
                    classroomName,
                    false
                )

                if (::originalClassScheduleDao.isInitialized && !isOffline) {
                    originalClassScheduleDao.insert(originalScheduleClass)
                }

                if (::adjustedClassScheduleDao.isInitialized) {
                    val adjustedClass = adjustedClassScheduleDao.get(originalScheduleClass.uid)
                    if (adjustedClass != null){
                        originalScheduleClass.courseName = adjustedClass.courseName
                        originalScheduleClass.group = adjustedClass.group
                        originalScheduleClass.teacherName = adjustedClass.teacherName
                        originalScheduleClass.buildingName = adjustedClass.buildingName
                        originalScheduleClass.classroomName = adjustedClass.classroomName
                        originalScheduleClass.dayIndex = adjustedClass.dayIndex
                        originalScheduleClass.startHour = adjustedClass.startHour
                        originalScheduleClass.finishHour = adjustedClass.finishHour
                    }
                }

                if (originalScheduleClass.startHour < minHour) {
                    minHour = originalScheduleClass.startHour
                }

                if (originalScheduleClass.finishHour > maxHour) {
                    maxHour = originalScheduleClass.finishHour
                }

                if (originalScheduleClass.finishHour - originalScheduleClass.startHour <= 1.0) {
                    classSize = 100
                }

                hasClass = true

                classesData.add(originalScheduleClass)
            }
        }

        fun addClass(originalScheduleClass: OriginalScheduleClass) {
            if(originalScheduleClass.uid.trim().isEmpty())return

            val color = if (courseUsedKeys.contains(originalScheduleClass.courseName)) {
                courseUsedColors[courseUsedKeys.indexOf(originalScheduleClass.courseName)]
            } else {
                courseUsedKeys.add(originalScheduleClass.courseName)
                courseUsedColors.add(resourceColors[colorIndex++])
                courseUsedColors.last()
            }

            originalScheduleClass.color = color

            if (::originalClassScheduleDao.isInitialized && !isOffline) {
                originalClassScheduleDao.insert(originalScheduleClass)
            }

            if (::adjustedClassScheduleDao.isInitialized) {
                val adjustedScheduleClass = adjustedClassScheduleDao.get(originalScheduleClass.uid)
                if (adjustedScheduleClass != null) {
                    originalScheduleClass.courseName = adjustedScheduleClass.courseName
                    originalScheduleClass.group = adjustedScheduleClass.group
                    originalScheduleClass.teacherName = adjustedScheduleClass.teacherName
                    originalScheduleClass.buildingName = adjustedScheduleClass.buildingName
                    originalScheduleClass.classroomName = adjustedScheduleClass.classroomName
                    originalScheduleClass.dayIndex = adjustedScheduleClass.dayIndex
                    originalScheduleClass.startHour = adjustedScheduleClass.startHour
                    originalScheduleClass.finishHour = adjustedScheduleClass.finishHour
                }
            }

            if (originalScheduleClass.startHour < minHour) {
                minHour = originalScheduleClass.startHour
            }

            if (originalScheduleClass.finishHour > maxHour) {
                maxHour = originalScheduleClass.finishHour
            }

            if (originalScheduleClass.finishHour - originalScheduleClass.startHour <= 1.0) {
                classSize = 100
            }

            hasClass = true

            classesData.add(originalScheduleClass)
        }

        @SuppressLint("SetTextI18n")
        @JavascriptInterface
        fun onScheduleFinished() {
            if(isDatabaseEnabled && ::personalClassScheduleDao.isInitialized){
                val all = personalClassScheduleDao.getAll()

                for (scheduleClass in all){
                    addClass(scheduleClass.toOriginalScheduleClass())
                }
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



            for(scheduleClass in classesData){
                activity?.runOnUiThread {
                    dayLayouts[scheduleClass.dayIndex].addView(
                        ScheduleClassView(
                            activity,
                            scheduleClass,
                            this@ScheduleView
                        )
                    )
                }
            }

            courseUsedKeys.clear()
            courseUsedColors.clear()
            colorIndex = 0

            if (activity is SAESActivity && isDatabaseEnabled) {
                (activity as SAESActivity).updateWidgets()
            }

            val horaSize =
                LinearLayout.LayoutParams(dpToPixel(context, 45), dpToPixel(context, classSize - 1))

            val dividerParams =
                LinearLayout.LayoutParams(dpToPixel(context, 45), dpToPixel(context, 1))
            val dividerColor = ContextCompat.getColor(context, R.color.colorDivider)
            val textColor = ContextCompat.getColor(context, R.color.colorTextPrimary)

            minHour = minHour.toInt().toDouble()
            maxHour = maxHour.toInt().toDouble() + offsetBottom

            activity?.runOnUiThread {
                for (i in minHour.toInt()..maxHour.toInt()) {
                    val textView = TextView(context)
                    textView.text = "$i:00"
                    textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    textView.layoutParams = horaSize
                    textView.setTextColor(textColor)

                    val divider = View(context)
                    divider.layoutParams = dividerParams
                    divider.setBackgroundColor(dividerColor)

                    mainView.hoursContainer.addView(divider)
                    mainView.hoursContainer.addView(textView)
                }

                mainView.scheduleProgressBar.visibility = View.GONE
            }

            classesData.clear()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        tickHandler = null
    }
}