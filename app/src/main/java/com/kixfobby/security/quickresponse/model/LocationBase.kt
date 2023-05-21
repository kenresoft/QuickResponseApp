package com.kixfobby.security.quickresponse.model

import java.io.Serializable

class LocationBase(val location: String, val time: String) : Serializable {
    fun equals(l: LocationBase): Boolean {
        return l.time == time && l.location == location
    }
}