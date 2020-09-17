package ziox.ramiro.saes.views.scheduleview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.view_schedule_evento.view.*
import kotlinx.android.synthetic.main.view_schedule_hora.view.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.dialogs.VerETSDialog
import ziox.ramiro.saes.utils.dpToPixel


const val OFFSET = 440f

class HoraView: FrameLayout{
    private lateinit var layout : View
    private lateinit var hora : ScheduleView.HoraData

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, hora : ScheduleView.HoraData) : super(context) {
        this.hora = hora
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    private fun initView(){
        layout = LayoutInflater.from(context).inflate(R.layout.view_schedule_hora, this, true)
        layout.horaLabel.text = hora.hora
        layout.meridianLabel.text = hora.meridian

        for(event in hora.eventos){
            val eventLayout = LayoutInflater.from(context).inflate(R.layout.view_schedule_evento, null, false)

            eventLayout.eventoTitle.text = event.title
            eventLayout.setOnClickListener {
                if(context is FragmentActivity){
                    VerETSDialog.newInstance(event)
                    VerETSDialog().show((context as FragmentActivity).supportFragmentManager, "ets_dialog")
                }
            }

            layout.eventosLayout.addView(eventLayout)
        }

        this.viewTreeObserver.addOnScrollChangedListener {
            val parentHeight = layout.eventosLayout.measuredHeight
            val changelayoutHeight = layout.changingLabelLayout.measuredHeight
            val relativeToTop = getRelativeTop(layout.changingLabelLayout)

            if(relativeToTop < OFFSET){
                Log.d("HoraViewOffset", "$relativeToTop  --- ${OFFSET - relativeToTop.toFloat()} -- $parentHeight - $changelayoutHeight = ${parentHeight - changelayoutHeight}")

                if(OFFSET - relativeToTop.toFloat() >= parentHeight - changelayoutHeight - dpToPixel(context, 4)){
                    layout.changingLabelLayout.translationY = parentHeight - changelayoutHeight.toFloat() - dpToPixel(context, 4)
                }else{
                    layout.changingLabelLayout.translationY = OFFSET - relativeToTop.toFloat()
                }
            }else{
                layout.changingLabelLayout.translationY = 0f
            }
        }
    }

    private fun getRelativeTop(viewTop: View): Int {
        return if (viewTop.parent === viewTop.rootView) {
            viewTop.top
        } else {
            viewTop.top + getRelativeTop(viewTop.parent as View)
        }
    }
}