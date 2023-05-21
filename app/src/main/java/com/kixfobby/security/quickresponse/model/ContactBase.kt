package com.kixfobby.security.quickresponse.model

import java.io.Serializable

class ContactBase : Serializable {
    var number: String
    var name: String?
    private var uid: String

    constructor(number: String, name: String?) {
        this.number = number
        this.name = name
        uid = ""
    }

    constructor(number: String, name: String?, uid: String) {
        this.number = number
        this.name = name
        this.uid = uid
    }

    @JvmName("getNumber1")
    fun getNumber(): String {
        return trim(number).trim { it <= ' ' }
    }

    @JvmName("getName1")
    fun getName(): String {
        return name!!.trim { it <= ' ' }
    }

    fun getUid(): String {
        return uid.trim { it <= ' ' }
    }

    fun setUid(uid: String) {
        this.uid = uid
    }

    fun trim(str: String): String {
        return str.replace("\\s".toRegex(), "")
    }

    fun equals(p: ContactBase): Boolean {
        return p.getName() == name && p.getNumber() == number
    } //&& p.getUid().equals(uid)
}