package com.kixfobby.security.badge

import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.kixfobby.security.badge.listener.OnBadgeCountChangeListener
import com.kixfobby.security.badge.util.DensityUtils

class ImageBadgeView : AppCompatImageView {
    private var manager: DrawerManager? = null
    private var onBadgeCountChangeListener: OnBadgeCountChangeListener? = null

    constructor(context: Context?) : super(context!!) {
        initAttr(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttr(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        initAttr(attrs)
    }

    private fun initAttr(attrs: AttributeSet?) {
        manager = DrawerManager(this@ImageBadgeView, attrs)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        manager!!.drawBadge(canvas)
    }

    /**
     * Register a callback to be invoked when counter of a badge changed.
     * @param listener the callback that will run.
     */
    fun setOnBadgeCountChangeListener(listener: OnBadgeCountChangeListener?) {
        onBadgeCountChangeListener = listener
    }

    /**
     * @return the badge counter value
     */
    var badgeValue: String? = null
        get() = manager!!.badge.value

    /**
     * Set a badge counter value and add change listener if it registered
     * @param badgeValue new counter value
     */
    fun setBadgeValue(badgeValue: String?): ImageBadgeView {
        manager!!.badge.setValue(badgeValue)
        if (onBadgeCountChangeListener != null) {
            onBadgeCountChangeListener!!.onCountChange(badgeValue)
        }
        invalidate()
        return this
    }

    /**
     * Get the maximum value of a badge (by default setting 99)
     */
    val maxBadgeValue: Int
        get() = manager!!.badge.maxValue

    /**
     * Set the maximum value of a badge (by default setting 99)
     * @param maxBadgeValue new maximum value for counter
     */
    fun setMaxBadgeValue(maxBadgeValue: Int): ImageBadgeView {
        manager!!.badge.maxValue = maxBadgeValue
        invalidate()
        return this
    }

    /**
     * @return state of a badge visible
     */
    val isVisibleBadge: Boolean
        get() = manager!!.badge.isVisible

    /**
     * Set a badge visible state
     * @param  visible If state true a badge will be visible
     */
    fun visibleBadge(visible: Boolean): ImageBadgeView {
        manager!!.badge.isVisible = visible
        invalidate()
        return this
    }

    /**
     * @return state of a badge vertical stretched
     */
    val isRoundBadge: Boolean
        get() = manager!!.badge.isRoundBadge

    /**
     * Define whether a badge can be stretched vertically
     * @param roundBadge If param is true, a badge can be stretched vertically
     */
    fun setRoundBadge(roundBadge: Boolean): ImageBadgeView {
        manager!!.badge.isRoundBadge = roundBadge
        invalidate()
        return this
    }

    /**
     * @return state of a badge fixed radius by width
     */
    val isFixedRadius: Boolean
        get() = manager!!.badge.isFixedRadius

    /**
     * Define whether a badge can be with fixed radius by width.
     * Badge can have only circle or square form.
     * @param fixedRadius If param is true, a badge radius fixed
     */
    fun setFixedRadius(fixedRadius: Boolean): ImageBadgeView {
        manager!!.badge.isFixedRadius = fixedRadius
        invalidate()
        return this
    }

    /**
     * State of a badge whether a badge can be oval form after first value number of counter
     * @return state of a badge
     */
    val isBadgeOvalAfterFirst: Boolean
        get() = manager!!.badge.isOvalAfterFirst

    /**
     * Define whether a badge can be oval form after first value number of counter
     * Please use this method only for custom drawable badge background. See [.setBadgeBackground].
     * @param badgeOvalAfterFirst If param is true, a badge can be oval form after first value
     */
    fun setBadgeOvalAfterFirst(badgeOvalAfterFirst: Boolean): ImageBadgeView {
        manager!!.badge.isOvalAfterFirst = badgeOvalAfterFirst
        invalidate()
        return this
    }

    /**
     * @return State whether the counter is showing
     */
    val isShowCounter: Boolean
        get() = manager!!.badge.isShowCounter

    /**
     * Specify whether the counter can be showing on a badge
     * @param showCounter Specify whether the counter is shown
     */
    fun setShowCounter(showCounter: Boolean): ImageBadgeView {
        manager!!.badge.isShowCounter = showCounter
        invalidate()
        return this
    }

    /**
     * State of a badge has limit counter
     * @return state of a badge has limit
     */
    val isLimitValue: Boolean
        get() = manager!!.badge.isLimitValue

    /**
     * Define whether a badge counter can have limit
     * @param badgeValueLimit If param is true, after max value (default 99) a badge will have counter 99+
     * Otherwise a badge will show the current value, e.g 101
     */
    fun setLimitBadgeValue(badgeValueLimit: Boolean): ImageBadgeView {
        manager!!.badge.isLimitValue = badgeValueLimit
        invalidate()
        return this
    }

    /**
     * Get the current badge background color
     */
    val badgeColor: Int
        get() = manager!!.badge.badgeColor

    /**
     * Set the background color for a badge
     * @param badgeColor the color of the background
     */
    fun setBadgeColor(badgeColor: Int): ImageBadgeView {
        manager!!.badge.badgeColor = badgeColor
        invalidate()
        return this
    }

    /**
     * Get the current text color of a badge
     */
    val badgeTextColor: Int
        get() = manager!!.badge.badgeTextColor

    /**
     * Set the text color for a badge
     * @param badgeTextColor the color of a badge text
     */
    fun setBadgeTextColor(badgeTextColor: Int): ImageBadgeView {
        manager!!.badge.badgeTextColor = badgeTextColor
        invalidate()
        return this
    }

    /**
     * Get the current text size of a badge
     */
    val badgeTextSize: Float
        get() = manager!!.badge.badgeTextSize

    /**
     * Set the text size for a badge
     * @param badgeTextSize the size of a badge text
     */
    fun setBadgeTextSize(badgeTextSize: Float): ImageBadgeView {
        manager!!.badge.badgeTextSize = DensityUtils.dpToPx(badgeTextSize)
        invalidate()
        return this
    }

    /**
     * Get padding of a badge
     */
    val badgePadding: Float
        get() = manager!!.badge.padding

    /**
     * Set a badge padding
     * @param badgePadding the badge padding
     */
    fun setBadgePadding(badgePadding: Int): ImageBadgeView {
        manager!!.badge.padding = DensityUtils.txtPxToSp(badgePadding.toFloat())
        invalidate()
        return this
    }

    /**
     * Get a badge radius
     */
    val badgeRadius: Float
        get() = manager!!.badge.radius

    /**
     * Set the badge fixed radius. Radius will not respond to changes padding or width of text.
     * @param fixedBadgeRadius badge fixed radius value
     */
    fun setFixedBadgeRadius(fixedBadgeRadius: Float): ImageBadgeView {
        manager!!.badge.fixedRadiusSize = fixedBadgeRadius
        invalidate()
        return this
    }

    /**
     * Get the current typeface [Typeface] of a badge
     */
    val badgeTextFont: Typeface?
        get() = manager!!.badge.badgeTextFont

    /**
     * Set the typeface for a badge text
     * @param font Font for a badge text
     */
    fun setBadgeTextFont(font: Typeface?): ImageBadgeView {
        manager!!.badge.badgeTextFont = font
        invalidate()
        return this
    }

    /**
     * Get the style of the badge text. Matches the [Typeface] text style
     */
    val badgeTextStyle: Int
        get() = manager!!.badge.textStyle

    /**
     * Set the style of the badge text. Matches the [Typeface] text style
     * @param badgeTextStyle Can be normal, bold, italic, bold_italic
     */
    fun setBadgeTextStyle(badgeTextStyle: Int): ImageBadgeView {
        manager!!.badge.textStyle = badgeTextStyle
        invalidate()
        return this
    }

    /**
     * Get the background of a badge.
     */
    val badgeBackground: Int
        get() = manager!!.badge.badgeBackground

    /**
     * Get the background of a badge [Drawable] or null.
     */
    val badgeBackgroundDrawable: Drawable?
        get() = manager!!.badge.backgroundDrawable

    /**
     * Set the custom background of a badge
     * @param badgeBackground the [Drawable] background of a badge from resources
     */
    fun setBadgeBackground(badgeBackground: Drawable?): ImageBadgeView {
        manager!!.badge.backgroundDrawable = badgeBackground
        invalidate()
        return this
    }

    /**
     * Clear counter badge value
     */
    fun clearBadge() {
        manager!!.badge.clearValue()
        invalidate()
    }

    /**
     * Get position of a badge [BadgePosition].
     * @return [BadgePosition] position on ImageView by index
     */
    val badgePosition: Int
        get() = manager!!.badge.position

    /**
     * Set badge position [BadgePosition] on ImageView
     * @param position on this view [BadgePosition] top_left, top_right, bottom_left, bottom_right
     */
    fun setBadgePosition(position: Int): ImageBadgeView {
        manager!!.badge.position = position
        invalidate()
        return this
    }
}