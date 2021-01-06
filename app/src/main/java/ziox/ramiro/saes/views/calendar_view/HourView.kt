package ziox.ramiro.saes.views.calendar_view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import ziox.ramiro.saes.databinding.ViewCalendarEventBinding
import ziox.ramiro.saes.databinding.ViewCalendarHourBinding
import ziox.ramiro.saes.dialogs.ETSDataDialogFragment
import ziox.ramiro.saes.utils.dpToPixel


const val OFFSET = 440f

class HourView: FrameLayout{
    private lateinit var layout : ViewCalendarHourBinding
    private lateinit var hora : CalendarView.HoraData

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, hora : CalendarView.HoraData) : super(context) {
        this.hora = hora
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    private fun initView(){
        layout = ViewCalendarHourBinding.inflate(LayoutInflater.from(context), this, true)
        layout.horaLabel.text = hora.hora
        layout.meridianLabel.text = hora.meridian

        for(event in hora.eventos){
            val eventLayout = ViewCalendarEventBinding.inflate(LayoutInflater.from(context))

            eventLayout.eventoTitle.text = event.title
            eventLayout.root.setOnClickListener {
                if(context is FragmentActivity){
                    ETSDataDialogFragment.newInstance(event)
                    ETSDataDialogFragment().show((context as FragmentActivity).supportFragmentManager, "ets_dialog")
                }
            }

            layout.eventosLayout.addView(eventLayout.root)
        }

        this.viewTreeObserver.addOnScrollChangedListener {
            val parentHeight = layout.eventosLayout.measuredHeight
            val changeLayoutHeight = layout.changingLabelLayout.measuredHeight
            val relativeToTop = getRelativeTop(layout.changingLabelLayout)

            if(relativeToTop < OFFSET){
                Log.d("HoraViewOffset", "$relativeToTop  --- ${OFFSET - relativeToTop.toFloat()} -- $parentHeight - $changeLayoutHeight = ${parentHeight - changeLayoutHeight}")

                if(OFFSET - relativeToTop.toFloat() >= parentHeight - changeLayoutHeight - dpToPixel(context, 4)){
                    layout.changingLabelLayout.translationY = parentHeight - changeLayoutHeight.toFloat() - dpToPixel(context, 4)
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