package ziox.ramiro.saes.activities

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.github.florent37.viewtooltip.ViewTooltip
import ziox.ramiro.saes.R
import ziox.ramiro.saes.databinding.ActivitySettingsBinding
import ziox.ramiro.saes.dialogs.OfflineSectionListDialogFragment
import ziox.ramiro.saes.utils.*


/**
 * Creado por Ramiro el 15/07/2018 a las 09:55 PM para SAES.
 */

class SettingsActivity : AppCompatActivity(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private var tooltip: ViewTooltip.TooltipView? = null
    private val crashlytics = FirebaseCrashlytics.getInstance()
    private lateinit var binding: ActivitySettingsBinding

    companion object {
        private const val WIDGET_CALIBRATION_RANGE = 150
    }

    val sectionIds = arrayOf(
        R.id.nav_kardex,
        R.id.nav_estado_general,
        R.id.nav_horario,
        R.id.nav_calendario_trabajo,
        R.id.nav_calific,
        R.id.nav_reinsc
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initTheme(this)
        setLightStatusBar(this)
        binding.parentLayout.addBottomInsetPadding()
        initToolbar()
        initSpinners()
        initSeekBar()

        binding.offlineEnablerSwitch.isChecked = getPreference(this, "offline_switch", false)
        binding.automaticScheduleExpandSwitch.isChecked = getPreference(this, "horario_expand", false)

        binding.automaticScheduleExpandSwitch.setOnCheckedChangeListener(this)
        binding.offlineEnablerSwitch.setOnCheckedChangeListener(this)
        binding.offlineSectionsListButton.setOnClickListener(this)
        binding.widgetRangeTooltip.setOnClickListener(this)
    }

    private fun initToolbar(){
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        binding.toolbar.setNavigationOnClickListener {
            crashlytics.log("Click en BackButton en la clase ${this.localClassName}")
            finish()
        }
        binding.toolbar.title = "Configuraciones"
    }

    private fun initSeekBar(){
        binding.scheduleWidgetCalibrator.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                setSeekBarProgress(p1)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {
                setPreference(this@SettingsActivity, "widget_nivel", binding.scheduleWidgetCalibrator.progress - WIDGET_CALIBRATION_RANGE)
            }
        })

        binding.scheduleWidgetCalibrator.max = WIDGET_CALIBRATION_RANGE * 2
        binding.scheduleWidgetCalibrator.progress = getPreference(this,"widget_nivel", 0) + WIDGET_CALIBRATION_RANGE
        setSeekBarProgress(binding.scheduleWidgetCalibrator.progress)
    }

    private fun setSeekBarProgress(progress : Int){
        val sign = if (progress - WIDGET_CALIBRATION_RANGE > 0) {
            "+"
        } else {
            ""
        }
        val value = progress - WIDGET_CALIBRATION_RANGE
        binding.calibratorCountTextView.text = "$sign$value"
    }

    private fun initSpinners(){
        val sectionId = getPreference(
            this,
            "seccion_inicio_v2",
            resources.getResourceEntryName(R.id.nav_kardex)
        ) as String
        var isInitDark = false

        initSpinner(
            this,
            binding.scheduleUpdateIntervalSpinner,
            arrayOf(
                "15 minutos",
                "20 minutos",
                "25 minutos",
                "30 minutos",
                "35 minutos",
                "40 minutos",
                "45 minutos"
            ),
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    setPreference(this@SettingsActivity, "widget_small_interval", (p2 * 5)+15)
                }
            })

        initSpinner(this, binding.startSectionSpinner, arrayOf(
            "Kárdex",
            "Estado General",
            "Horario",
            "Calendario de trabajo",
            "Calificaciones",
            "Cita de reinscripción"
        ), object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                setPreference(
                    this@SettingsActivity,
                    "seccion_inicio_v2",
                    resources.getResourceEntryName(sectionIds[position])
                )
            }
        })

        initSpinner(this, binding.themeSelectorSpinner, resources.getStringArray(
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
                R.array.themeOptionsApi29
            } else {
                R.array.themeOptionsApi28
            }
        ), object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (isInitDark) {
                    try {
                        setPreference(this@SettingsActivity, "dark_mode", when (p2) {
                            0 -> {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                                AppCompatDelegate.MODE_NIGHT_NO
                            }
                            1 -> {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                                AppCompatDelegate.MODE_NIGHT_YES
                            }
                            else -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                                } else {
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                                }
                            }
                        })
                    } catch (e: Exception) {
                        Log.e(this.javaClass.canonicalName, e.toString())
                    }
                } else {
                    isInitDark = true
                }
            }
        })


        binding.themeSelectorSpinner.setSelection(
            when (getPreference(
                this,
                "dark_mode",
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                } else {
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                }
            )) {
                AppCompatDelegate.MODE_NIGHT_NO -> 0
                AppCompatDelegate.MODE_NIGHT_YES -> 1
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> 2
                AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY -> 2
                else -> 0
            }
        )

        binding.startSectionSpinner.setSelection(
            sectionIds.indexOf(
                resources.getIdentifier(
                    sectionId,
                    "id",
                    packageName
                )
            )
        )

        binding.scheduleUpdateIntervalSpinner.setSelection((getPreference(this,"widget_small_interval", 15)-15) / 5)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        crashlytics.log("Configuracion cambiada en ${this.localClassName}:\n$newConfig")
    }

    override fun onClick(view: View) {
        crashlytics.log("Click en ${resources.getResourceName(view.id)} en la clase ${this.localClassName}")
        when(view.id){
            R.id.widget_range_tooltip -> {
                if (tooltip == null) {
                    tooltip = ViewTooltip.on(this, view).corner(30).position(ViewTooltip.Position.TOP)
                        .autoHide(false, 0).clickToHide(true)
                        .text("Debido a la gran variedad de pantallas es posible que las clases no cuadren en el horario. Puedes arreglarlo subiendo o bajando el tamaño de estas.")
                        .color(Color.parseColor("#7e1210"))
                        .show()

                    tooltip?.setListenerHide {
                        tooltip = null
                    }
                }
            }
            R.id.offline_sections_list_button ->  {
                OfflineSectionListDialogFragment().show(supportFragmentManager, "offline_list")
            }
        }
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, enabled: Boolean) {
        when(compoundButton.id){
            R.id.automatic_schedule_expand_switch -> {
                setPreference(this, "horario_expand", enabled)
            }
            R.id.offline_enabler_switch -> {
                setPreference(this, "offline_switch", enabled)
            }
        }
    }
}