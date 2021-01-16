package ziox.ramiro.saes.views.tic_tac_toe

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updateLayoutParams
import ziox.ramiro.saes.R
import ziox.ramiro.saes.databinding.ViewTicTacToeBinding
import kotlin.random.Random


class TicTacToeView : FrameLayout, View.OnClickListener {
    private lateinit var binding : ViewTicTacToeBinding
    private var boardPositions : Array<Array<Boolean?>> = Array(3){ Array(3){ null } }
    private lateinit var boardCellViews : Array<ImageView>
    private var crossTurn = true
        set(value) {
            field = value
            val color = ContextCompat.getColor(
                context, if (value) {
                    R.color.colorInfo
                }else{
                    R.color.colorDanger
                }
            )

            if (::binding.isInitialized){
                binding.titleImageView.setImageDrawable(if (value) {
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_circle, context.theme)
                }else{
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_close_black_24dp,
                        context.theme
                    )
                })
                binding.titleTextView.setTextColor(color)
                binding.titleImageView.imageTintList = ColorStateList.valueOf(color)
                binding.titleTextView.text = "Juegas "
            }
        }
    private var player = false
    private var opponent = true
    private var winner : Boolean? = null
        set(value) {
            field = value
            val color = ContextCompat.getColor(
                context,
                when (value) {
                    true -> {
                        R.color.colorInfo
                    }
                    false -> {
                        R.color.colorDanger
                    }
                    else -> {
                        R.color.colorTextPrimary
                    }
                }
            )

            binding.titleImageView.setImageDrawable(
                when (value) {
                    true -> {
                        ResourcesCompat.getDrawable(resources, R.drawable.ic_circle, context.theme)
                    }
                    false -> {
                        ResourcesCompat.getDrawable(resources, R.drawable.ic_close_black_24dp, context.theme)
                    }
                    else -> {
                        null
                    }
                }
            )

            binding.titleImageView.imageTintList = ColorStateList.valueOf(color)
            binding.titleTextView.setTextColor(color)
            binding.titleTextView.text =  if (value == null){
                "Empate "
            }else{
                "Gana "
            }
        }
    private var miniMax = MiniMax(opponent, player)
    private var isGameOver = false
        set(value) {
            field = value
            if (value){
                if (::binding.isInitialized){
                    binding.restartButton.visibility = View.VISIBLE
                }
            }else{
                if (::binding.isInitialized){
                    binding.restartButton.visibility = View.INVISIBLE
                }
                initBoard()
            }
        }

    constructor(context: Context) : super(context){
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        initView()
    }

    private fun initView(){
        binding = ViewTicTacToeBinding.inflate(LayoutInflater.from(context), this, true)
        boardCellViews = arrayOf(
            binding.position1,
            binding.position2,
            binding.position3,
            binding.position4,
            binding.position5,
            binding.position6,
            binding.position7,
            binding.position8,
            binding.position9
        )

        binding.restartButton.setOnClickListener {
            isGameOver = false
        }

        boardCellViews.forEach {
            it.setOnClickListener(this)
        }

        var globalLayoutListener : ViewTreeObserver.OnGlobalLayoutListener? = null

        globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            val set = ConstraintSet()
            set.clone(binding.root)
            set.constrainHeight(R.id.ticTacToeContainer, binding.ticTacToeContainer.width)
            set.applyTo(binding.root)

            binding.ticTacToeContainer.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        }

        binding.ticTacToeContainer.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)

        initBoard()
    }

    private fun initBoard(){
        Thread{
            boardPositions = Array(3){ Array(3){ null } }
            player = !player
            opponent = !opponent
            winner = null
            crossTurn = true
            boardCellViews.forEach {
                if (context is Activity){
                    (context as Activity).runOnUiThread {
                        it.setImageDrawable(null)
                    }
                }
            }
            miniMax = MiniMax(opponent, player)
            if (opponent){
                aiPlay(Random.nextInt(3), Random.nextInt(3))
            }
        }.run()
    }

    private fun checkTile(x: Int, y: Int, isOs: Boolean?) : Boolean{
        if (isTileMarked(y,x)) return false
        boardCellViews[y.times(3).plus(x)].setImageDrawable(
            when (isOs) {
                true -> {
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_circle, context.theme)
                }
                false -> {
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_close_black_24dp,
                        context.theme
                    )
                }
                else -> {
                    null
                }
            }
        )
        boardCellViews[y.times(3).plus(x)].imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                context,
                when (isOs) {
                    true -> {
                        R.color.colorInfo
                    }
                    false -> {
                        R.color.colorDanger
                    }
                    else -> {
                        R.color.colorTextPrimary
                    }
                }
            )
        )

        boardPositions[y][x] = isOs
        return true
    }

    private fun checkGameOver() {
        if (miniMax.evaluate(boardPositions) != 0 || !miniMax.isMovesLeft(boardPositions)){
            winner = when(miniMax.evaluate(boardPositions)){
                10 -> opponent
                -10 -> player
                else -> null
            }
            isGameOver = true
        }
    }

    private fun isTileMarked(y: Int, x: Int) = boardPositions[y][x] != null

    override fun onClick(view: View) {
        val isLegalMove = if(crossTurn == player && !isGameOver){
            when(view.id){
                R.id.position1 -> checkTile(0, 0, player)
                R.id.position2 -> checkTile(1, 0, player)
                R.id.position3 -> checkTile(2, 0, player)
                R.id.position4 -> checkTile(0, 1, player)
                R.id.position5 -> checkTile(1, 1, player)
                R.id.position6 -> checkTile(2, 1, player)
                R.id.position7 -> checkTile(0, 2, player)
                R.id.position8 -> checkTile(1, 2, player)
                R.id.position9 -> checkTile(2, 2, player)
                else -> false
            }
        }else{
            false
        }

        if(isLegalMove){
            crossTurn = opponent
            checkGameOver()
            aiPlay()
        }
    }

    private fun aiPlay(x: Int? = null, y: Int? = null){
        Thread{
            val move = miniMax.findBestMove(boardPositions)
            if(move != null){
                if(context is Activity){
                    (context as Activity).runOnUiThread {
                        checkTile(x ?: move.x, y ?: move.y, opponent)
                    }
                }
                crossTurn = player
            }
        }.run()
        checkGameOver()
    }
}