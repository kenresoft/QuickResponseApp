package com.kixfobby.security.quickresponse.model

class UsersDb {
    var email: String? = null
    var clickCount: Int? = null

    constructor() {}
    constructor(clickCount: Int?) {
        this.clickCount = clickCount
    }

    constructor(email: String?, clickCount: Int?) {
        this.email = email
        this.clickCount = clickCount
    }
}