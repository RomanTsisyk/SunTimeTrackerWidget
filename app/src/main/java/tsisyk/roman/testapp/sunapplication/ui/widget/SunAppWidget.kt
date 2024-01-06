package tsisyk.roman.testapp.sunapplication.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget

import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.appwidget.provideContent


class SunAppWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Text(
                text = "Sunrise/Sunset Widget",
                modifier = GlanceModifier.fillMaxSize()
            )
        }
    }
}

