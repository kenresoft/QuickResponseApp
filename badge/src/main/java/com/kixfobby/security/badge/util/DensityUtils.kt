package com.kixfobby.security.badge.util

import android.content.res.Resources
import android.util.TypedValue

/**
 * Util methods for converting data value
 * @author Ivan V on 19.02.2018.
 * @version 1.0
 */
object DensityUtils {
    fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().displayMetrics)
    }

    fun pxToDp(px: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, Resources.getSystem().displayMetrics)
    }

    fun txtPxToSp(px: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, px, Resources.getSystem().displayMetrics)
    }
}