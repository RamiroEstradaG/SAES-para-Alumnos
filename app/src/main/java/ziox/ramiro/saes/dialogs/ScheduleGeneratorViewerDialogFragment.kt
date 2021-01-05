package ziox.ramiro.saes.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ziox.ramiro.saes.databases.AppLocalDatabase
import ziox.ramiro.saes.databases.ScheduleClass
import ziox.ramiro.saes.databases.ScheduleGeneratorDao
import ziox.ramiro.saes.databinding.DialogFragmentScheduleViewerBinding
import ziox.ramiro.saes.utils.dividirHoras

/**
 * Creado por Ramiro el 1/23/2019 a las 5:17 PM para SAESv2.
 */
class ScheduleGeneratorViewerDialogFragment : DialogFragment() {
    lateinit var rootView: DialogFragmentScheduleViewerBinding
    private lateinit var scheduleGeneratorDao: ScheduleGeneratorDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Dialog_NoActionBar_MinWidth)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = DialogFragmentScheduleViewerBinding.inflate(inflater, container, false)
        scheduleGeneratorDao = AppLocalDatabase.getInstance(requireContext()).scheduleGeneratorDao()
        val scheduleData = ArrayList<ScheduleClass>()

        val items = scheduleGeneratorDao.getAll()

        for (item in items){
            for (i in 0 until 5) {
                val horas = dividirHoras(
                    when (i) {
                        0 -> item.monday
                        1 -> item.tuesday
                        2 -> item.wednesday
                        3 -> item.thursday
                        4 -> item.friday
                        else -> item.monday
                    }
                )

                if (horas != null) {
                    scheduleData.add(
                        ScheduleClass(
                            (item.group + item.courseName).replace(" ", "_"),
                            i,
                            item.courseName,
                            horas.first,
                            horas.second,
                            "#000000",
                            item.group,
                            item.teacherName,
                            item.buildingName,
                            item.classroomName,
                            false
                        )
                    )
                }
            }
        }

        rootView.horarioDialog.loadData(scheduleData.map { it.toOriginalScheduleClass() }.toTypedArray(), activity)
        return rootView.root
    }
}