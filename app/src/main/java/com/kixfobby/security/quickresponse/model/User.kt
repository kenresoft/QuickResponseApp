package com.kixfobby.security.quickresponse.model

import android.graphics.Bitmap
import android.graphics.drawable.VectorDrawable
import android.widget.ImageView
import com.kixfobby.security.quickresponse.util.FBaseUtil.compressBitmapToByteArray
import com.kixfobby.security.quickresponse.util.FBaseUtil.encodeByteToStringBase64
import com.kixfobby.security.quickresponse.util.FBaseUtil.getBitmapFromAImageView
import java.io.Serializable

class User : Serializable {
    var name: String? = null
        private set
    var profilePicture: String? = null
        private set
    var uid: String? = null
        private set
    var email: String? = null
        private set
    var phone: String? = null
        private set

    fun setName(name: String?): User {
        this.name = name
        return this
    }

    fun setEmail(email: String?): User {
        this.email = email
        return this
    }

    fun setProfilePicture(profilePicture: ImageView): User {
        if (profilePicture.drawable is VectorDrawable) {
            this.profilePicture = null
            return this
        }
        val bitmap = getBitmapFromAImageView(profilePicture)
        val bytes = compressBitmapToByteArray(bitmap, Bitmap.CompressFormat.PNG, 100)
        this.profilePicture = encodeByteToStringBase64(bytes)
        return this
    }

    fun setUid(uid: String?): User {
        //this.uid = String.valueOf(new Random().nextLong());
        this.uid = uid
        return this
    }

    fun setPhone(phone: String?): User {
        this.phone = phone
        return this
    }
}