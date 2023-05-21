package com.kixfobby.security.quickresponse.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

object ContextUtil {
    fun getActivity(context: Context): Activity? {
        var context = context
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }
}