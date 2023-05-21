package com.kixfobby.security.quickresponse.model

import java.io.Serializable

class ProfileBase(
    val email: String,
    val pin: String,
    val phone: String,
    val country: String,
    val state: String,
    val zip: String,
    val kin1: String,
    val kin2: String,
    val kin3: String,
    val num1: String,
    val num2: String,
    val num3: String
) : Serializable