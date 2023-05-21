package com.kixfobby.security.quickresponse.util

import top.defaults.camera.Size

class SizeItem(private val size: Size) : PickerItemWrapper<Size?> {
    override fun getText(): String {
        return size.width.toString() + " * " + size.height
    }

    override fun get(): Size {
        return size
    }
}