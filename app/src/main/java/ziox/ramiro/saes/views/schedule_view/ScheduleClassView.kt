package ziox.ramiro.saes.views.schedule_view

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ziox.ramiro.saes.R
import ziox.ramiro.saes.databases.ScheduleClass
import ziox.ramiro.saes.databinding.ViewScheduleClassBinding
import ziox.ramiro.saes.utils.dpToPixel
import ziox.ramiro.saes.utils.getInitials
import ziox.ramiro.saes.utils.toProperCase

/**
 * Creado por Ramiro el 12/4/2018 a las 7:45 PM para SAESv2.
 */
class ScheduleClassView : CardView {
    private lateinit var classData: ScheduleClass
    private lateinit var scheduleView: ScheduleView
    private lateinit var mainView: ViewScheduleClassBinding
    private var isClassExpanded = false
    private var activity: FragmentActivity? = null
    private var isUserCustomClass = false
    private val crashlytics = FirebaseCrashlytics.getInstance()

    constructor(context: Context) : super(context) {
        initLayout()
    }

    constructor(activity: FragmentActivity?, classData: ScheduleClass, scheduleView: ScheduleView, isUserCustomClass : Boolean = false) : super(activity!!.applicationContext) {
        this.activity = activity
        this.classData = classData
        this.scheduleView = scheduleView
        this.isUserCustomClass = isUserCustomClass
        initLayout()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initLayout()
    }

    private fun initLayout() {
        mainView = ViewScheduleClassBinding.inflate(LayoutInflater.from(context), this, true)

        if (scheduleView.isExpanded && classData.dayIndex == scheduleView.selectedIndex) {
            expand()
        } else {
            collapse()
        }

        mainView.buildingNameTextView.text = classData.buildingName
        mainView.classRoomNameTextView.text = classData.classroomName
        mainView.teacherNameTextView.text = classData.teacherName.toProperCase()

        val layoutTransition = LayoutTransition()
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        layoutTransition.setDuration(300)
        this.layoutTransition = layoutTransition

        val classLayoutSize = dpToPixel(context, scheduleView.classSize)
        val layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            (classLayoutSize * (classData.finishHour - classData.startHour)).toInt()
        )

        this.setCardBackgroundColor(Color.parseColor(classData.color))

        layoutParams.setMargins(
            0,
            ((classData.startHour - scheduleView.minHour) * classLayoutSize).toInt(),
            0,
            0
        )
        activity?.runOnUiThread {
            this.layoutParams = layoutParams
        }

        scheduleView.addOnChangeListener{ isExpanded, index ->
            if (classData.dayIndex == index) {
                if (isExpanded) {
                    expand()
                } else {
                    collapse()
                }
            } else {
                collapse()
            }
        }

        mainView.editButton.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            scheduleView.openBottomSheetForEditClass(classData)
        }

        mainView.removeButton.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            val dialog = AlertDialog.Builder(activity as Context, R.style.DialogAlert)
            dialog.setTitle("Eliminar clase")
            dialog.setMessage("¿Estás seguro de eliminar ${classData.courseName.toProperCase()}?")
            dialog.setPositiveButton("Eliminar"){ _, _ ->
                scheduleView.removeUserCustomClass(classData)
            }
            dialog.setNegativeButton("Cancelar"){ _, _ -> }
            dialog.show()
        }

        if(!classData.isUserPersonalClass && scheduleView.canEdit){
            mainView.removeButton.visibility = View.GONE
        }
    }

    private fun expand() {
        mainView.courseNameTextView.text = classData.courseName.toProperCase()
        mainView.courseNameTextView.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START

        mainView.buildingNameTitle.visibility = View.VISIBLE
        mainView.classroomNameTitle.visibility = View.VISIBLE
        mainView.moreInfoLayout.visibility = View.VISIBLE

        mainView.importantDataLayout.orientation = LinearLayout.HORIZONTAL

        if (scheduleView.canEdit) {
            mainView.editButton.visibility = View.VISIBLE
        }

        if(classData.isUserPersonalClass && scheduleView.canEdit){
            mainView.removeButton.visibility = View.VISIBLE
        }

        isClassExpanded = true
    }

    private fun collapse() {
        mainView.courseNameTextView.text = classData.courseName.getInitials()
        mainView.courseNameTextView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

        mainView.buildingNameTitle.visibility = View.GONE
        mainView.classroomNameTitle.visibility = View.GONE
        mainView.moreInfoLayout.visibility = View.GONE

        mainView.importantDataLayout.orientation = LinearLayout.VERTICAL

        mainView.editButton.visibility = View.GONE
        mainView.removeButton.visibility = View.GONE

        isClassExpanded = false
    }
}