package ziox.ramiro.saes.views.selectable_button_view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.Button
import androidx.core.content.ContextCompat
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton
import ziox.ramiro.saes.R
import ziox.ramiro.saes.databinding.ViewSelectableButtonBinding
import ziox.ramiro.saes.utils.toProperCase

class SelectableButtonView : FlexboxLayout{
    lateinit var onItemSelectedListener : AdapterView.OnItemSelectedListener
    private lateinit var data : Array<String>
    var selectedIndex : Int = -1

    var selectedItem : Button? = null
    private var buttonColor : Int = 0
    var isSelectOnInitEnable = true
    var isKeepIndexEnable = false
    var padStart = 0
    var padEnd = 0

    constructor(context: Context) : super(context){
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        initView()
    }

    private fun initView(){
        buttonColor = ContextCompat.getColor(context, R.color.colorInfo)
        this.flexDirection = FlexDirection.ROW
        this.flexWrap = FlexWrap.WRAP
        this.alignItems = AlignItems.STRETCH
    }

    fun setOptions(data: Array<String>){
        clean()
        this.data = data.clone()

        for((i, name) in data.copyOfRange(padStart, data.size - padEnd).withIndex()){
            val optionButton = ViewSelectableButtonBinding.inflate(LayoutInflater.from(context)).itemeSelectableButton

            optionButton.text = name.toProperCase()
            optionButton.setTextColor(buttonColor)
            (optionButton as MaterialButton).strokeColor = ColorStateList.valueOf(buttonColor)

            optionButton.setOnClickListener {
                setSelection(i)
            }

            this.addView(optionButton)

            if(isKeepIndexEnable && selectedIndex == i){
                setSelection(i)
            }else if(i == 0 && isSelectOnInitEnable && selectedIndex == -1){
                setSelection(0)
            }
        }
    }

    fun clean(){
        if(!isKeepIndexEnable){
            this.selectedIndex = -1
        }
        this.selectedItem = null
        this.data = ArrayList<String>().toTypedArray()
        this.removeAllViews()
    }


    fun setSelection(index: Int){
        selectedItem?.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        selectedItem?.setTextColor(buttonColor)

        val child = this.getChildAt(index) as Button?

        if(::onItemSelectedListener.isInitialized){
            onItemSelectedListener.onItemSelected(null, child, index+padStart, (index + padStart).toLong())
        }

        child?.backgroundTintList = ColorStateList.valueOf(buttonColor)
        child?.setTextColor(ContextCompat.getColor(context, R.color.colorOnPrimary))

        selectedIndex = index + padStart
        selectedItem = child
    }
}