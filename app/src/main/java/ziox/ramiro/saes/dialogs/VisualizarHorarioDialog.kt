package ziox.ramiro.saes.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_visualizar_materias.view.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.databases.HorarioGeneradoDatabase
import ziox.ramiro.saes.utils.ClaseData
import ziox.ramiro.saes.utils.dividirHoras

/**
 * Creado por Ramiro el 1/23/2019 a las 5:17 PM para SAESv2.
 */
class VisualizarHorarioDialog : DialogFragment() {
    lateinit var rootView: View
    private lateinit var horarioPersonalizadoDatabase: HorarioGeneradoDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Dialog_NoActionBar_MinWidth)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.dialog_visualizar_materias, container, false)
        horarioPersonalizadoDatabase = HorarioGeneradoDatabase(activity)
        val horarioData = ArrayList<ClaseData>()

        val cursor = horarioPersonalizadoDatabase.getAll()

        while (cursor.moveToNext()) {
            val data = HorarioGeneradoDatabase.cursorAsClaseData(cursor)

            for (i in 0 until 5) {
                val horas = dividirHoras(
                    when (i) {
                        0 -> data.lunes
                        1 -> data.martes
                        2 -> data.miercoles
                        3 -> data.jueves
                        4 -> data.viernes
                        else -> data.lunes
                    }
                )

                if (horas != null) {
                    horarioData.add(
                        ClaseData(
                            (data.grupo + data.materia).replace(" ", "_"),
                            i,
                            data.materia,
                            horas.first,
                            horas.second,
                            "#000000",
                            data.grupo,
                            data.profesor,
                            data.edificio,
                            data.salon
                        )
                    )
                }
            }
        }

        cursor.close()

        rootView.horarioDialog.loadData(horarioData.toTypedArray(), activity)
        return rootView
    }

    override fun onDestroy() {
        super.onDestroy()
        rootView.horarioDialog.closeDatabases()
        if (::horarioPersonalizadoDatabase.isInitialized) {
            horarioPersonalizadoDatabase.close()
        }
    }
}