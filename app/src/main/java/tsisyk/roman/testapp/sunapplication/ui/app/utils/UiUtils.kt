package tsisyk.roman.testapp.sunapplication.ui.app.utils

import android.content.Context
import android.widget.Toast

object UiUtils {
    fun showShortToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
