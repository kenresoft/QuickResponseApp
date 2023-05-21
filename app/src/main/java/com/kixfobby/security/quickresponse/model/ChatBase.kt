package com.kixfobby.security.quickresponse.model

import java.io.Serializable

class ChatBase : Serializable {
    var number: String
        private set
    var name: String
        private set
    var message: String
        private set
    var dt: String
    private var uid: String

    constructor(number: String, name: String, message: String, dt: String) {
        this.number = number
        this.name = name
        this.message = message
        this.dt = dt
        uid = ""
    }

    constructor(number: String, name: String, message: String, dt: String, uid: String) {
        this.number = number
        this.name = name
        this.message = message
        this.dt = dt
        this.uid = uid
    }

    fun getUid(): String {
        return uid.trim { it <= ' ' }
    }

    fun setUid(uid: String) {
        this.uid = uid
    }

    fun getDT(): String {
        return dt
    }

    fun equals(s: ChatBase): Boolean {
        return s.name == name && s.message == message && s.number == number && s.dt == dt
    }
}