package tsisyk.roman.testapp.sunapplication.provider


import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import tsisyk.roman.testapp.sunapplication.ui.widget.SunAppWidget

class SunAppWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SunAppWidget()
}