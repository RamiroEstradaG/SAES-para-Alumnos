package ziox.ramiro.saes.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_fragment_licencias.view.*
import ziox.ramiro.saes.R

/**
 * Creado por Ramiro el 7/30/2018 a las 7:59 PM para SAES.
 */
class FragmentDialogLicencias : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Dialog_NoActionBar_MinWidth)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_fragment_licencias, container, false)

        rootView.wv_licencia.loadDataWithBaseURL(null, "<style>" +
                "   a{text-decoration: none;color:#FF1744;text-overflow: ellipsis;display: inline-block;max-width: 100%;overflow: hidden;white-space: nowrap;vertical-align: top} " +
                "</style>" +
                "<div>" +
                "<b>Android Support Library</b><br>" +
                "Copyright © 2005 The Android Open Source Project. All rights reserved.<br>" +
                "<a href='http://developer.android.com/tools/extras/support-library.html'>http://developer.android.com/tools/extras/support-library.html</a>" +
                "</div><br>" +
                "<div>" +
                "<b>Google Material Design Icons</b><br>" +
                "Copyright © Google, Inc. All rights reserved.<br>" +
                "<a href='https://material.io/icons/'>https://material.io/icons/</a>" +
                "</div><br>" +
                "<div>" +
                "<b>Expandable Layout Library</b><br>" +
                "Copyright © 2016 Daniel Cachapa. All rights reserved.<br>" +
                "<a href='https://github.com/cachapa/ExpandableLayout'>https://github.com/cachapa/ExpandableLayout</a>" +
                "</div><br>" +
                "<div>" +
                "<b>Material Design Demo</b><br>" +
                "Copyright © 2016 Eajy. All rights reserved.<br>" +
                "<a href='https://github.com/Eajy/MaterialDesignDemo'>https://github.com/Eajy/MaterialDesignDemo</a>" +
                "</div><br>" +
                "<div>" +
                "<b>Android In-App Billing v3 Library</b><br>" +
                "Copyright © 2014 AnjLab. All rights reserved.<br>" +
                "<a href='https://github.com/anjlab/android-inapp-billing-v3'>https://github.com/anjlab/android-inapp-billing-v3</a>" +
                "</div><br>" +
                "<div>" +
                "<b>ViewTooltip Library</b><br>" +
                "Copyright © 2017 Florent37, Inc. All rights reserved.<br>" +
                "<a href='https://github.com/florent37/ViewTooltip'>https://github.com/florent37/ViewTooltip</a>" +
                "</div><br>" +
                "<div>" +
                "<b>Flexbox Layout Library</b><br>" +
                "Copyright © 2018 Google LLC. All rights reserved.<br>" +
                "<a href='https://github.com/google/flexbox-layout'>https://github.com/google/flexbox-layout</a>" +
                "</div><br>" +
                "<div>" +
                "<b>Android Debug Database Library</b><br>" +
                "Copyright © 2019 Amit Shekhar. All rights reserved.<br>" +
                "<a href='https://github.com/amitshekhariitbhu/Android-Debug-Database'>https://github.com/amitshekhariitbhu/Android-Debug-Database</a>" +
                "</div><br>" +
                "<div>" +
                "<b>MPAndroidChart Library</b><br>" +
                "Copyright © 2019 Philipp Jahoda. All rights reserved.<br>" +
                "<a href='https://github.com/PhilJay/MPAndroidChart#documentation'>https://github.com/PhilJay/MPAndroidChart#documentation</a>" +
                "</div><br>" +
                "<div>" +
                "<b>Kotlin Programming Language, Kotlin Libraries and Kotlin Compiler</b><br>" +
                "Copyright © 2000-2019 JetBrains s.r.o. and Kotlin Programming Language contributors. All rights reserved.<br>" +
                "<a href='https://github.com/JetBrains/kotlin'>https://github.com/JetBrains/kotlin</a>" +
                "</div><br>" +
                "<div>" +
                "<b>CompactCalendarView</b><br>" +
                "Copyright © 2018 Sundeepk. All rights reserved.<br>" +
                "<a href='https://github.com/SundeepK/CompactCalendarView'>https://github.com/SundeepK/CompactCalendarView</a>" +
                "</div><br>" +
                "<div>" +
                "<b>MPAndroidChart Library</b><br>" +
                "Copyright © 2019 Philipp Jahoda. All rights reserved.<br>" +
                "<a href='https://github.com/PhilJay/MPAndroidChart#documentation'>https://github.com/PhilJay/MPAndroidChart#documentation</a>" +
                "</div><br>" +
                "<div>" +
                "<b>Sistema de Administración Escolar (SAES) and webpage resources</b><br>" +
                "Copyright © 2008 IPN, IPN-DAE. All rights reserved.<br>" +
                "<a href='http://www.ipn.mx/'>http://www.ipn.mx/</a>" +
                "</div><br>" +
                "<div>", "text/html", "utf-8", null)

        return rootView
    }
}