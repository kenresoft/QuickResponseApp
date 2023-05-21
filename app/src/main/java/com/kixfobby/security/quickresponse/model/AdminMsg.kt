package com.kixfobby.security.quickresponse.model

class AdminMsg() {
    var title: String? = null
    var message: String? = null
    var time: String? = null
    var seen: Boolean? = false
    var expanded: Boolean? = false

    fun setTitle(title: String?): AdminMsg {
        this.title = title
        return this
    }

    fun setMessage(message: String?): AdminMsg {
        this.message = message
        return this
    }

    fun setTime(time: String?): AdminMsg {
        this.time = time
        return this
    }

    fun setSeen(seen: Boolean?): AdminMsg {
        this.seen = seen
        return this
    }

}
