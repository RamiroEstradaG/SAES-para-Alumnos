package ziox.ramiro.saes.activities

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.github.florent37.viewtooltip.ViewTooltip
import kotlinx.android.synthetic.main.activity_settings.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.dialogs.FragmentDialogOfflineList
import ziox.ramiro.saes.utils.*


/**
 * Creado por Ramiro el 15/07/2018 a las 09:55 PM para SAES.
 */

class SettingsActivity : AppCompatActivity() {
    private var tooltip: ViewTooltip.TooltipView? = null
    private val crashlytics = FirebaseCrashlytics.getInstance()

    companion object {
        private const val WIDGET_CALIBRAR_RANGO = 150
    }

    val seccionId = arrayOf(
        R.id.nav_kardex,
        R.id.nav_estado_general,
        R.id.nav_horario,
        R.id.nav_calendario_trabajo,
        R.id.nav_calific,
        R.id.nav_reinsc
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initTheme(this)
        setLightStatusBar(this)
        parentLayout.addBottomInsetPadding()

        this.initActivity()

        val pref = this.applicationContext.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        seekBarWidgetLevel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                textViewWidgetCounter.text = "${if (p1 - WIDGET_CALIBRAR_RANGO >= 0) {
                    "+"
                } else {
                    ""
                }}${p1 - WIDGET_CALIBRAR_RANGO}"
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {
                pref.edit()
                    .putInt("widget_nivel", seekBarWidgetLevel.progress - WIDGET_CALIBRAR_RANGO)
                    .apply()
            }
        })

        switch_offline.isChecked = pref.getBoolean("offline_switch", false)
        switch_expand.isChecked = pref.getBoolean("horario_expand", false)

        btnMoreOffline.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.localClassName}")
            FragmentDialogOfflineList().show(supportFragmentManager, "offline_list")
        }
    }

    private fun initActivity() {
        toolbar2.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        toolbar2.setNavigationOnClickListener {
            crashlytics.log("Click en BackButton en la clase ${this.localClassName}")
            finish()
        }
        toolbar2.title = "Configuraciones"


        val pref = this.applicationContext.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        var isInitDark = false

        initSpinner(this, switch_dark_theme, resources.getStringArray(
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
                        pref.edit().putInt(
                            "dark_mode", when (p2) {
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
                            }
                        ).apply()
                    } catch (e: Exception) {
                        Log.e("AppException", e.toString())
                    }
                } else {
                    isInitDark = true
                }
            }
        })

        switch_dark_theme.setSelection(
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


        initSpinner(
            this,
            spinnerMinsInterval,
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
                    pref.edit().putInt("widget_small_interval", (p2 * 5)+15).apply()
                }
            })

        switch_expand.setOnCheckedChangeListener { _, b ->
            pref.edit().putBoolean("horario_expand", b).apply()
        }
        switch_offline.setOnCheckedChangeListener { _, b ->
            pref.edit().putBoolean("offline_switch", b).apply()
        }

        spinnerMinsInterval.setSelection((pref.getInt("widget_small_interval", 15)-15) / 5)

        initSpinner(this, spinner_seccion, arrayOf(
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
                    resources.getResourceEntryName(seccionId[position])
                )
            }
        })

        val seccionIdName = getPreference(
            this,
            "seccion_inicio_v2",
            resources.getResourceEntryName(R.id.nav_kardex)
        ) as String

        spinner_seccion.setSelection(
            seccionId.indexOf(
                resources.getIdentifier(
                    seccionIdName,
                    "id",
                    packageName
                )
            )
        )

        tooltipWidget.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.localClassName}")
            if (tooltip == null) {
                tooltip = ViewTooltip.on(this, it).corner(30).position(ViewTooltip.Position.TOP)
                    .autoHide(false, 0).clickToHide(true)
                    .text("Debido a la gran variedad de pantallas es posible que las clases no cuadren en el horario. Puedes arreglarlo subiendo o bajando el tamaño de estas.")
                    .color(Color.parseColor("#7e1210"))
                    .show()

                tooltip?.setListenerHide {
                    tooltip = null
                }
            }
        }

        seekBarWidgetLevel.max = WIDGET_CALIBRAR_RANGO * 2
        seekBarWidgetLevel.progress = pref.getInt("widget_nivel", 0) + WIDGET_CALIBRAR_RANGO
        textViewWidgetCounter.text = if (seekBarWidgetLevel.progress - WIDGET_CALIBRAR_RANGO > 0) {
            "+"
        } else {
            ""
        } + (seekBarWidgetLevel.progress - WIDGET_CALIBRAR_RANGO).toString()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        crashlytics.log("Configuracion cambiada en ${this.localClassName}:\n$newConfig")
    }
}