package ziox.ramiro.saes.activities

import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.android.material.snackbar.Snackbar
import ziox.ramiro.saes.R
import ziox.ramiro.saes.databases.AppLocalDatabase
import ziox.ramiro.saes.databases.ScheduleGeneratorClass
import ziox.ramiro.saes.databases.ScheduleGeneratorDao
import ziox.ramiro.saes.databinding.ActivityScheduleGeneratorBinding
import ziox.ramiro.saes.databinding.ViewScheduleGeneratorItemBinding
import ziox.ramiro.saes.dialogs.AddCourseDialogFragment
import ziox.ramiro.saes.dialogs.ScheduleGeneratorViewerDialogFragment
import ziox.ramiro.saes.utils.*
import kotlin.collections.ArrayList

/**
 * Creado por Ramiro el 1/19/2019 a las 4:44 AM para SAESv2.
 */
class ScheduleGeneratorActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var scheduleGeneratorDao: ScheduleGeneratorDao
    lateinit var adapter: ScheduleGeneratorAdapter
    private val crashlytics = FirebaseCrashlytics.getInstance()
    private lateinit var binding: ActivityScheduleGeneratorBinding

    private val classes = ArrayList<ScheduleGeneratorClass>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initTheme(this)
        setSystemUiLightStatusBar(this, false)
        binding.itemContainer.addBottomInsetPadding{
            binding.addItemFab.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                bottomMargin += EDGE_INSET_BOTTOM
            }
        }

        scheduleGeneratorDao = AppLocalDatabase.getInstance(this).scheduleGeneratorDao()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        adapter = ScheduleGeneratorAdapter()

        setupRecyclerView()

        classes.addAll(scheduleGeneratorDao.getAll())

        binding.itemContainer.adapter?.notifyDataSetChanged()

        binding.addItemFab.setOnClickListener(this)
        binding.previewClassScheduleFab.setOnClickListener(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupRecyclerView(){
        val simpleItemTouchCallback = CustomTouchHelper(adapter, scheduleGeneratorDao)

        binding.itemContainer.adapter = adapter
        binding.itemContainer.layoutManager = LinearLayoutManager(this)

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.itemContainer)
    }

    override fun onClick(view: View) {
        crashlytics.log("Click en ${resources.getResourceName(view.id)} en la clase ${this.localClassName}")
        when(view.id){
            R.id.add_item_fab -> {

                if (this.isNetworkAvailable()) {
                    AddCourseDialogFragment().show(supportFragmentManager, "dialog_anadir_materia")
                } else {
                    Snackbar.make(view, "No tienes conexión a internet.", Snackbar.LENGTH_LONG).show()
                }
            }
            R.id.preview_class_schedule_fab -> {
                ScheduleGeneratorViewerDialogFragment().show(supportFragmentManager, "dialog_preview_materia")
            }
        }
    }

    fun addItemAndDismiss(newItem: ScheduleGeneratorClass) {
        if (scheduleGeneratorDao.getByCourseName(newItem.courseName).isEmpty()) {
            val items = scheduleGeneratorDao.getAll()
            val hourInterference = ArrayList<String>()
            var error = false

            for (item in items){
                var exist = false
                for (i in 0..4) {
                    val newItemHour = dividirHoras(
                        when (i) {
                            0 -> newItem.monday
                            1 -> newItem.tuesday
                            2 -> newItem.wednesday
                            3 -> newItem.thursday
                            4 -> newItem.friday
                            else -> ""
                        }
                    )
                    val previouslyAddedItemHour = dividirHoras(
                        when (i) {
                            0 -> item.monday
                            1 -> item.tuesday
                            2 -> item.wednesday
                            3 -> item.thursday
                            4 -> item.friday
                            else -> ""
                        }
                    )
                    if (newItemHour != null && previouslyAddedItemHour != null) {
                        if (newItemHour.first in previouslyAddedItemHour.first..(previouslyAddedItemHour.second - 0.2) ||
                            newItemHour.second - 0.2 in previouslyAddedItemHour.first..(previouslyAddedItemHour.second - 0.2) ||
                            previouslyAddedItemHour.first in newItemHour.first..(newItemHour.second - 0.2) ||
                            previouslyAddedItemHour.second - 0.2 in newItemHour.first..(newItemHour.second - 0.2)
                        ) {
                            exist = true
                            error = true
                        }
                    }
                }
                if (exist) {
                    hourInterference.add(item.courseName.toProperCase())
                }
            }

            if (error) {
                Toast.makeText(
                    this,
                    hourInterference.joinToString(
                        prefix = "Esta materia interfiere con: ",
                        postfix = "."
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                (supportFragmentManager.findFragmentByTag("dialog_anadir_materia") as AddCourseDialogFragment?)?.dismiss()
                val rowId = scheduleGeneratorDao.insert(newItem)
                val databaseResult = scheduleGeneratorDao.get(rowId)
                if (databaseResult != null) {
                    (binding.itemContainer.adapter as ScheduleGeneratorAdapter).addItem(databaseResult)
                }
            }

        } else {
            Toast.makeText(this, "La materia ya fue agregada.", Toast.LENGTH_LONG).show()
        }
    }

    inner class ScheduleGeneratorAdapter : RecyclerView.Adapter<ScheduleGeneratorAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(ViewScheduleGeneratorItemBinding.inflate(layoutInflater, parent, false))
        }

        override fun getItemCount() = classes.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = classes[position]

            holder.title.text = item.courseName.toProperCase()
            holder.teacherName.text = item.teacherName.toProperCase()
            holder.group.text = item.group
            holder.careerName.text = item.careerName.toProperCase()
            holder.uid = item.uid

            Log.d(this.javaClass.canonicalName, "Holder UID: ${holder.uid}")
        }

        override fun getItemId(position: Int) = classes[position].uid.toLong()

        fun addItem(insertData: ScheduleGeneratorClass, position: Int = classes.size) {
            classes.add(position, insertData)
            runOnUiThread {
                notifyItemInserted(position)
            }
        }

        fun itemDismiss(position: Int) {
            classes.removeAt(position)
            runOnUiThread {
                notifyItemRemoved(position)
            }

            Snackbar.make(binding.container, "Se ha eliminado la materia.", Snackbar.LENGTH_LONG).show()
        }

        inner class ViewHolder constructor(private val itemBinding: ViewScheduleGeneratorItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
            val title = itemBinding.courseNameTextView
            val teacherName = itemBinding.teacherNameTextView
            val group = itemBinding.groupTextView
            val careerName = itemBinding.careerNameTextView
            var uid : Int = -1

            fun getSwipeView(): View {
                return itemBinding.itemForeground
            }
        }
    }

    inner class CustomTouchHelper(val adapter : ScheduleGeneratorActivity.ScheduleGeneratorAdapter, private val scheduleGeneratorDao: ScheduleGeneratorDao) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        override fun onSwiped(holder: RecyclerView.ViewHolder, p1: Int) {
            if(holder is ScheduleGeneratorActivity.ScheduleGeneratorAdapter.ViewHolder){
                Log.d(this.javaClass.canonicalName, "Delete Holder UID: ${holder.uid}")
                if(scheduleGeneratorDao.deleteByUid(holder.uid) > 0){
                    adapter.itemDismiss(holder.adapterPosition)
                }else{
                    Snackbar.make(binding.container, "No se ha podido eliminar la materia", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        override fun onMove(
            p0: RecyclerView,
            p1: RecyclerView.ViewHolder,
            p2: RecyclerView.ViewHolder
        ): Boolean = true

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return if (viewHolder is ScheduleGeneratorActivity.ScheduleGeneratorAdapter.ViewHolder) {
                val swipeFlags = ItemTouchHelper.RIGHT
                makeMovementFlags(0, swipeFlags)
            } else
                0
        }

        override fun clearView(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ) {
            getDefaultUIUtil().clearView((viewHolder as ScheduleGeneratorActivity.ScheduleGeneratorAdapter.ViewHolder).getSwipeView())
        }

        override fun onSelectedChanged(
            viewHolder: RecyclerView.ViewHolder?,
            actionState: Int
        ) {
            if (viewHolder != null) {
                getDefaultUIUtil().onSelected((viewHolder as ScheduleGeneratorActivity.ScheduleGeneratorAdapter.ViewHolder).getSwipeView())
            }
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            getDefaultUIUtil().onDraw(
                c,
                recyclerView,
                (viewHolder as ScheduleGeneratorActivity.ScheduleGeneratorAdapter.ViewHolder).getSwipeView(),
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
        }

        override fun onChildDrawOver(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            getDefaultUIUtil().onDrawOver(
                c,
                recyclerView,
                (viewHolder as ScheduleGeneratorActivity.ScheduleGeneratorAdapter.ViewHolder).getSwipeView(),
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
        }
    }
}