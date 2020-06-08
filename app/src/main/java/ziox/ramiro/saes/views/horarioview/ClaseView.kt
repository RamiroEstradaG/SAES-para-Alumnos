package ziox.ramiro.saes.views.horarioview

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
import kotlinx.android.synthetic.main.view_clase.view.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.utils.ClaseData
import ziox.ramiro.saes.utils.dpToPixel
import ziox.ramiro.saes.utils.getInitials
import ziox.ramiro.saes.utils.toProperCase

/**
 * Creado por Ramiro el 12/4/2018 a las 7:45 PM para SAESv2.
 */
class ClaseView : CardView {
    private lateinit var claseData: ClaseData
    private lateinit var horarioView: HorarioView
    private lateinit var lay: View
    private var isClaseExpanded = false
    private var activity: FragmentActivity? = null
    private var isUserCustomClass = false
    private val crashlytics = FirebaseCrashlytics.getInstance()

    constructor(context: Context) : super(context) {
        initLayout()
    }

    constructor(activity: FragmentActivity?, clase: ClaseData, horarioView: HorarioView, isUserCustomClass : Boolean = false) : super(activity!!.applicationContext) {
        this.activity = activity
        this.claseData = clase
        this.horarioView = horarioView
        this.isUserCustomClass = isUserCustomClass
        initLayout()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initLayout()
    }

    private fun initLayout() {
        lay = LayoutInflater.from(context).inflate(R.layout.view_clase, this, true)

        if (horarioView.isExpanded && claseData.diaIndex == horarioView.selectedIndex) {
            expand()
        } else {
            collapse()
        }

        lay.claseEdificio.text = claseData.edificio
        lay.claseSalon.text = claseData.salon
        lay.claseProfe.text = claseData.profesor.toProperCase()

        val tran = LayoutTransition()
        tran.enableTransitionType(LayoutTransition.CHANGING)
        tran.setDuration(300)
        this.layoutTransition = tran

        val claseSize = dpToPixel(context, horarioView.claseSize)
        val layParms = LayoutParams(
            LayoutParams.MATCH_PARENT,
            (claseSize * (claseData.horaFinal - claseData.horaInicio)).toInt()
        )

        this.setCardBackgroundColor(Color.parseColor(claseData.color))

        layParms.setMargins(
            0,
            ((claseData.horaInicio - horarioView.minHora) * claseSize).toInt(),
            0,
            0
        )
        activity?.runOnUiThread {
            layoutParams = layParms
        }

        horarioView.addOnChangeListener{ isExpanded, index ->
            if (claseData.diaIndex == index) {
                if (isExpanded) {
                    expand()
                } else {
                    collapse()
                }
            } else {
                collapse()
            }
        }

        lay.claseEditButton.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            horarioView.editClase(claseData)
        }

        lay.claseRemoveButton.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            val dialog = AlertDialog.Builder(activity as Context, R.style.DialogAlert)
            dialog.setTitle("Eliminar clase")
            dialog.setMessage("¿Estás seguro de eliminar ${claseData.materia.toProperCase()}?")
            dialog.setPositiveButton("Ok"){ _, _ ->
                horarioView.removeUserCustomClase(claseData)
            }
            dialog.show()
        }

        if(!claseData.isUserCustomClase && horarioView.canEdit){
            lay.claseRemoveButton.visibility = View.GONE
        }
    }

    private fun expand() {
        lay.claseNombre.text = claseData.materia.toProperCase()
        lay.claseNombre.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START

        lay.fieldEdificio.visibility = View.VISIBLE
        lay.fieldSalon.visibility = View.VISIBLE
        lay.moreInfoLayout.visibility = View.VISIBLE

        lay.impDataLayout.orientation = LinearLayout.HORIZONTAL

        if (horarioView.canEdit) {
            lay.claseEditButton.visibility = View.VISIBLE
        }

        if(claseData.isUserCustomClase && horarioView.canEdit){
            lay.claseRemoveButton.visibility = View.VISIBLE
        }

        isClaseExpanded = true
    }

    private fun collapse() {
        lay.claseNombre.text = claseData.materia.getInitials()
        lay.claseNombre.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

        lay.fieldEdificio.visibility = View.GONE
        lay.fieldSalon.visibility = View.GONE
        lay.moreInfoLayout.visibility = View.GONE

        lay.impDataLayout.orientation = LinearLayout.VERTICAL

        lay.claseEditButton.visibility = View.GONE
        lay.claseRemoveButton.visibility = View.GONE

        isClaseExpanded = false
    }
}