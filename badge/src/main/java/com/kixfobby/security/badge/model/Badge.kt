package com.kixfobby.security.badge.model

import android.graphics.Typeface
import android.graphics.drawable.Drawable

/**
 * @author Ivan V on 21.02.2018.
 * @version 1.0
 */
class Badge {
    var value: String? = null
    var maxValue = 0
    var radius = 0f
    var fixedRadiusSize = 0f
    var badgeColor = 0
    var badgeTextColor = 0
    var badgeTextSize = 0f
    var padding = 0f
    var badgeTextFont: Typeface? = null
    var textStyle = 0
    var badgeBackground = 0
    var isVisible = false
    var isLimitValue = false
    var isRoundBadge = false
    var isFixedRadius = false
    var isOvalAfterFirst = false
    var isShowCounter = false
    var backgroundDrawable: Drawable? = null
    var textWidth = 0f
    var position = 0


    fun clearValue() {
        value = ""
    }

    fun setValue(value: String?): Badge {
        this.value = value
        return this
    }

    fun setMaxValue(maxValue: Int): Badge {
        this.maxValue = maxValue
        return this
    }

    fun setRadius(radius: Float): Badge {
        this.radius = radius
        return this
    }

    fun setFixedRadiusSize(fixedRadiusSize: Float): Badge {
        this.fixedRadiusSize = fixedRadiusSize
        return this
    }

    fun setBadgeColor(badgeColor: Int): Badge {
        this.badgeColor = badgeColor
        return this
    }

    fun setBadgeTextColor(badgeTextColor: Int): Badge {
        this.badgeTextColor = badgeTextColor
        return this
    }

    fun setBadgeTextSize(badgeTextSize: Float): Badge {
        this.badgeTextSize = badgeTextSize
        return this
    }

    fun setPadding(padding: Float): Badge {
        this.padding = padding
        return this
    }

    fun setBadgeTextFont(badgeTextFont: Typeface?): Badge {
        this.badgeTextFont = badgeTextFont
        return this
    }

    fun setTextStyle(textStyle: Int): Badge {
        this.textStyle = textStyle
        return this
    }

    fun setBadgeBackground(badgeBackground: Int): Badge {
        this.badgeBackground = badgeBackground
        return this
    }

    fun setVisible(visible: Boolean): Badge {
        isVisible = visible
        return this
    }

    fun setLimitValue(limitValue: Boolean): Badge {
        isLimitValue = limitValue
        return this
    }

    fun setRoundBadge(roundBadge: Boolean): Badge {
        isRoundBadge = roundBadge
        return this
    }

    fun setFixedRadius(fixedRadius: Boolean): Badge {
        isFixedRadius = fixedRadius
        return this
    }

    fun setOvalAfterFirst(ovalAfterFirst: Boolean): Badge {
        isOvalAfterFirst = ovalAfterFirst
        return this
    }

    fun setShowCounter(showCounter: Boolean): Badge {
        isShowCounter = showCounter
        return this
    }

    fun setBackgroundDrawable(backgroundDrawable: Drawable?): Badge {
        this.backgroundDrawable = backgroundDrawable
        return this
    }

    fun setTextWidth(textWidth: Float): Badge {
        this.textWidth = textWidth
        return this
    }

    fun setPosition(position: Int): Badge {
        this.position = position
        return this
    }
}