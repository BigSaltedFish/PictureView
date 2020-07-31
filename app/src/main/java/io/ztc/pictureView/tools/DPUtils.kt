package io.ztc.pictureView.tools

import android.content.Context
import android.util.TypedValue

object DPUtils{
    fun dpToPx(context: Context, dipValue: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dipValue.toFloat(), context.resources.displayMetrics).toInt()
    }

}
