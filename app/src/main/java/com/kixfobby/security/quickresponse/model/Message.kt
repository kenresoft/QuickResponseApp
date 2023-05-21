package com.kixfobby.security.quickresponse.model

class Message {
    var message: String? = null
        private set
    var userPicture: String? = null
        private set
    var receiverUid: String? = null
        private set
    var receiverName: String? = null
        private set
    var senderUid: String? = null
        private set
    var senderName: String? = null
        private set
    var email: String? = null
        private set
    var date: String? = null
        private set
    var messageStatus: String? = null
        private set
    var messageType: String? = null
        private set

    fun setMessage(message: String?): Message {
        this.message = message
        return this
    }

    fun setReceiverUid(receiverUid: String?): Message {
        this.receiverUid = receiverUid
        return this
    }

    fun setSenderUid(senderUid: String?): Message {
        this.senderUid = senderUid
        return this
    }

    fun setUserPicture(userPicture: String?): Message {
        this.userPicture = userPicture
        return this
    }

    fun setReceiverName(receiverName: String?): Message {
        this.receiverName = receiverName
        return this
    }

    fun setSenderName(senderName: String?): Message {
        this.senderName = senderName
        return this
    }

    fun setEmail(email: String?): Message {
        this.email = email
        return this
    }

    fun setDate(date: String?): Message {
        this.date = date
        return this
    }

    fun setMessageStatus(messageStatus: String?): Message {
        this.messageStatus = messageStatus
        return this
    }

    fun setMessageType(messageType: String?): Message {
        this.messageType = messageType
        return this
    }
}