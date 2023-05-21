package com.kixfobby.security.badge

import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import com.kixfobby.security.badge.constant.Constants
import com.kixfobby.security.badge.model.Badge
import com.kixfobby.security.badge.util.DensityUtils

class AttributeController(private val view: View, private val attrs: AttributeSet?) {
    /**
     * @return initialized badge and counter
     */
    val badge: Badge = Badge()
    private fun initAttr() {
        val typedArray = view.context.obtainStyledAttributes(attrs, R.styleable.ImageBadgeView)
        val value = typedArray.getString(R.styleable.ImageBadgeView_ibv_badgeValue)
        val maxBadgeValue = typedArray.getInt(R.styleable.ImageBadgeView_ibv_maxBadgeValue, Constants.MAX_VALUE)
        val textSize = typedArray.getDimension(
            R.styleable.ImageBadgeView_ibv_badgeTextSize, DensityUtils.txtPxToSp(
                Constants.DEFAULT_TEXT_SIZE.toFloat()
            )
        )
        val padding = typedArray.getDimension(
            R.styleable.ImageBadgeView_ibv_badgePadding, DensityUtils.pxToDp(
                Constants.DEFAULT_BADGE_PADDING.toFloat()
            )
        )
        val fixedBadgeRadius = typedArray.getDimension(
            R.styleable.ImageBadgeView_ibv_fixedBadgeRadius, DensityUtils.pxToDp(
                Constants.NO_INIT
            )
        )
        val badgeTextStyle =
            typedArray.getInt(R.styleable.ImageBadgeView_ibv_badgeTextStyle, Constants.DEFAULT_FONT_STYLE)
        val fontPath = typedArray.getString(R.styleable.ImageBadgeView_ibv_badgeTextFont)
        val badgeTextFont = if (fontPath != null) Typeface.createFromFile(fontPath) else Constants.DEFAULT_FONT
        val badgeDrawable = typedArray.getDrawable(R.styleable.ImageBadgeView_ibv_badgeBackground)
        val visible = typedArray.getBoolean(R.styleable.ImageBadgeView_ibv_visibleBadge, Constants.DEFAULT_VISIBLE)
        val limitValue = typedArray.getBoolean(R.styleable.ImageBadgeView_ibv_badgeLimitValue, Constants.DEFAULT_LIMIT)
        val roundBadge = typedArray.getBoolean(R.styleable.ImageBadgeView_ibv_roundBadge, Constants.DEFAULT_ROUND)
        val fixedRadius =
            typedArray.getBoolean(R.styleable.ImageBadgeView_ibv_fixedRadius, Constants.DEFAULT_FIXED_RADIUS)
        val ovalAfterFirst =
            typedArray.getBoolean(R.styleable.ImageBadgeView_ibv_badgeOvalAfterFirst, Constants.DEFAULT_BADGE_OVAL)
        val showCounter =
            typedArray.getBoolean(R.styleable.ImageBadgeView_ibv_showCounter, Constants.DEFAULT_SHOW_COUNTER)
        val badgeColor = typedArray.getColor(R.styleable.ImageBadgeView_ibv_badgeColor, Constants.DEFAULT_BADGE_COLOR)
        val badgeTextColor =
            typedArray.getColor(R.styleable.ImageBadgeView_ibv_badgeTextColor, Constants.DEFAULT_TEXT_COLOR)
        val badgePosition = typedArray.getInt(R.styleable.ImageBadgeView_ibv_badgePosition, BadgePosition.TOP_RIGHT)
        badge.setValue(value)
            .setMaxValue(maxBadgeValue)
            .setBadgeTextSize(textSize)
            .setPadding(padding)
            .setFixedRadiusSize(fixedBadgeRadius)
            .setTextStyle(badgeTextStyle)
            .setBadgeTextFont(badgeTextFont)
            .setBackgroundDrawable(badgeDrawable)
            .setVisible(visible)
            .setLimitValue(limitValue)
            .setRoundBadge(roundBadge)
            .setFixedRadius(fixedRadius)
            .setOvalAfterFirst(ovalAfterFirst)
            .setShowCounter(showCounter)
            .setBadgeColor(badgeColor)
            .setBadgeTextColor(badgeTextColor).position = badgePosition
        typedArray.recycle()
    }

    init {
        initAttr()
    }
}