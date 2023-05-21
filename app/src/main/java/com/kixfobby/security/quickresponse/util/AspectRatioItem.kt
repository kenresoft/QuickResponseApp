package com.kixfobby.security.quickresponse.util

import top.defaults.camera.AspectRatio

class AspectRatioItem(private val aspectRatio: AspectRatio) : PickerItemWrapper<AspectRatio?> {
    override fun getText(): String {
        return aspectRatio.toString()
    }

    override fun get(): AspectRatio {
        return aspectRatio
    }
}