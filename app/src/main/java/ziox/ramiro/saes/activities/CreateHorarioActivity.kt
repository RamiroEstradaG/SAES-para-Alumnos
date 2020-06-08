package ziox.ramiro.saes.activities

import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_create_horario.*
import kotlinx.android.synthetic.main.view_create_horario_item.view.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.dialogs.AnadirMateriaDialog
import ziox.ramiro.saes.dialogs.VisualizarHorarioDialog
import ziox.ramiro.saes.sql.HorarioGeneradoDatabase
import ziox.ramiro.saes.utils.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Creado por Ramiro el 1/19/2019 a las 4:44 AM para SAESv2.
 */
class CreateHorarioActivity : AppCompatActivity() {
    lateinit var horarioPersonalizadoDatabase: HorarioGeneradoDatabase
    lateinit var adapter: MateriaAdapter
    private val crashlytics = FirebaseCrashlytics.getInstance()

    private val materiasList = ArrayList<HorarioGeneradoDatabase.Data>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_horario)
        initTheme(this)
        setLightStatusBar(this)
        frameLayout.addBottomInsetPadding()
        recyclerViewCrearHorario.addBottomInsetPadding()

        horarioPersonalizadoDatabase = HorarioGeneradoDatabase(this)

        toolbarCrearHorario.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        toolbarCrearHorario.setNavigationOnClickListener {
            crashlytics.log("Click en BackButtonen la clase ${this.localClassName}")
            finish()
        }

        toolbarCrearHorario.title = "Generador de horario"

        adapter = MateriaAdapter()

        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {
                    adapter.itemDismiss(p0.adapterPosition)
                    horarioPersonalizadoDatabase.deleteMateriaByName(p0.itemView.crearHorarioMateria.text.toString().toUpperCase(Locale.ROOT))
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
                    return if (viewHolder is MateriaAdapter.ViewHolder) {
                        val swipeFlags = ItemTouchHelper.RIGHT
                        makeMovementFlags(0, swipeFlags)
                    } else
                        0
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    getDefaultUIUtil().clearView((viewHolder as MateriaAdapter.ViewHolder).getSwipeView())
                }

                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    if (viewHolder != null) {
                        getDefaultUIUtil().onSelected((viewHolder as MateriaAdapter.ViewHolder).getSwipeView())
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
                        (viewHolder as MateriaAdapter.ViewHolder).getSwipeView(),
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
                        (viewHolder as MateriaAdapter.ViewHolder).getSwipeView(),
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            }


        recyclerViewCrearHorario.adapter = adapter
        recyclerViewCrearHorario.layoutManager = LinearLayoutManager(this)

        val mItemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        mItemTouchHelper.attachToRecyclerView(recyclerViewCrearHorario)

        horarioPersonalizadoDatabase.createTable()

        val data = horarioPersonalizadoDatabase.getAll()

        while (data.moveToNext()) {
            materiasList.add(HorarioGeneradoDatabase.cursorAsClaseData(data))
        }

        recyclerViewCrearHorario.adapter?.notifyDataSetChanged()

        fabAnadirMateria.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.localClassName}")
            if (this.isNetworkAvailable()) {
                AnadirMateriaDialog().show(supportFragmentManager, "dialog_anadir_materia")
            } else {
                Snackbar.make(it, "No tienes conexión a internet.", Snackbar.LENGTH_LONG).show()
            }
        }

        fabPreviewMateria.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.localClassName}")
            VisualizarHorarioDialog().show(supportFragmentManager, "dialog_preview_materia")
        }
    }

    fun anadirMateriaAndDimiss(data: HorarioGeneradoDatabase.Data) {
        if (horarioPersonalizadoDatabase.getMateriaByName(data.materia).count == 0) {
            val all = horarioPersonalizadoDatabase.getAll()
            val cols = horarioPersonalizadoDatabase.col
            val colsArr =
                arrayOf(cols.lunes, cols.martes, cols.miercoles, cols.jueves, cols.viernes)
            val materiasInterfiere = ArrayList<String>()
            var error = false
            while (all.moveToNext()) {
                var exist = false
                for (i in 0 until 5) {
                    val horaS1 = dividirHoras(
                        when (i) {
                            0 -> data.lunes
                            1 -> data.martes
                            2 -> data.miercoles
                            3 -> data.jueves
                            4 -> data.viernes
                            else -> ""
                        }
                    )
                    val horaS2 = dividirHoras(all.getString(all.getColumnIndex(colsArr[i])))
                    if (horaS1 != null && horaS2 != null) {
                        if (horaS1.first in horaS2.first..(horaS2.second - 0.2) ||
                            horaS1.second - 0.2 in horaS2.first..(horaS2.second - 0.2) ||
                            horaS2.first in horaS1.first..(horaS1.second - 0.2) ||
                            horaS2.second - 0.2 in horaS1.first..(horaS1.second - 0.2)
                        ) {
                            exist = true
                            error = true
                        }
                    }
                }
                if (exist) {
                    materiasInterfiere.add(all.getString(all.getColumnIndex(cols.materia)).toProperCase())
                }
            }
            if (error) {
                Toast.makeText(
                    this,
                    materiasInterfiere.joinToString(
                        prefix = "Esta materia interfiere con: ",
                        postfix = "."
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                (supportFragmentManager.findFragmentByTag("dialog_anadir_materia") as AnadirMateriaDialog?)?.dismiss()
                horarioPersonalizadoDatabase.add(data)
                (recyclerViewCrearHorario.adapter as MateriaAdapter).addItem(
                    materiasList.size,
                    data
                )
            }

        } else {
            Toast.makeText(this, "La materia ya fue agregada.", Toast.LENGTH_LONG).show()
        }
    }

    inner class MateriaAdapter : RecyclerView.Adapter<MateriaAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.view_create_horario_item,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int {
            return materiasList.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val materia = materiasList[position]

            holder.titulo.text = materia.materia.toProperCase()
            holder.profesor.text = materia.profesor.toProperCase()
            holder.grupo.text = materia.grupo
            holder.carrera.text = materia.carrera.toProperCase()
        }

        fun addItem(position: Int, insertData: HorarioGeneradoDatabase.Data) {
            materiasList.add(position, insertData)
            runOnUiThread {
                notifyItemInserted(position)
            }
        }

        fun itemDismiss(position: Int) {
            materiasList.removeAt(position)
            runOnUiThread {
                notifyItemRemoved(position)
            }

            Snackbar.make(coordinatorCrearHorario, "Se ha eliminado la materia.", Snackbar.LENGTH_LONG).show()
        }

        inner class ViewHolder constructor(private val v: View) : RecyclerView.ViewHolder(v) {
            var titulo = TextView(this@CreateHorarioActivity)
            var profesor = TextView(this@CreateHorarioActivity)
            var grupo = TextView(this@CreateHorarioActivity)
            var carrera = TextView(this@CreateHorarioActivity)
            private var holderView = View(this@CreateHorarioActivity)

            init {
                titulo = v.crearHorarioMateria
                profesor = v.crearHorarioProfesor
                grupo = v.crearHorarioGrupo
                carrera = v.crearHorarioCarrera
                holderView = v
            }

            fun getSwipeView(): View {
                return v.itemForeground
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::horarioPersonalizadoDatabase.isInitialized) {
            horarioPersonalizadoDatabase.close()
        }
    }
}