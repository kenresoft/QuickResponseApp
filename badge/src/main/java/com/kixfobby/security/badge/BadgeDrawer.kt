package com.kixfobby.security.badge

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import com.kixfobby.security.badge.constant.Constants
import com.kixfobby.security.badge.model.Badge

/**
 * Draw a badge and the count on ImageView
 * @author Ivan V on 21.02.2018.
 * @version 1.0
 */
class BadgeDrawer internal constructor(private val view: View, private val badge: Badge) {
    private var position: BadgePosition? = null
    private var paint: Paint? = null

    /**
     * Draw badge and counter on [Canvas]
     * If a badge counter <= 0 it will be hided on [AppCompatImageView]
     */
    fun draw(canvas: Canvas) {
        if (badge.isVisible || badge.value !== "") {
            initPaint()
            computeTextWidth()
            position = BadgePosition(view, badge).compute()
            drawBadge(canvas)
            if (badge.isShowCounter) {
                drawText(canvas)
            }
        }
    }

    private fun initPaint() {
        if (paint == null) {
            paint = Paint()
            paint!!.isAntiAlias = true
            val typeface = Typeface.create(badge.badgeTextFont, badge.textStyle)
            paint!!.typeface = typeface
            paint!!.textSize = badge.badgeTextSize
        }
    }

    // Draw a badge
    private fun drawBadge(canvas: Canvas) {
        paint!!.color = badge.badgeColor
        if (badge.backgroundDrawable != null) {
            badge.backgroundDrawable!!.setBounds(
                0,
                0,
                position!!.badgeWidth.toInt(),
                position!!.badgeHeight.toInt()
            )
            canvas.save()
            canvas.translate(
                position!!.deltaX - position!!.badgeWidth / 2,
                position!!.deltaY - position!!.badgeHeight / 2
            )
            badge.backgroundDrawable!!.draw(canvas)
            canvas.restore()
        } else {
            canvas.drawCircle(position!!.deltaX, position!!.deltaY, badge.radius, paint!!)
        }
    }

    // Draw text
    private fun drawText(canvas: Canvas) {
        paint!!.color = badge.badgeTextColor
        if (badge.isLimitValue && badge.value == null) {
            canvas.drawText(
                (badge.maxValue + Constants.PLUS.toInt()).toString(),
                position!!.deltaX - badge.textWidth / 2,
                position!!.deltaY + badge.badgeTextSize / 3f, paint!!
            )
        } else {
            canvas.drawText(
                badge.value.toString(),
                position!!.deltaX - badge.textWidth / 2,
                position!!.deltaY + badge.badgeTextSize / 3f, paint!!
            )
        }
    }

    private fun computeTextWidth() {
        val textWidth: Float = if (badge.isLimitValue && badge.value == null) {
            paint!!.measureText((badge.maxValue + Constants.PLUS.toInt()).toString())
        } else {
            paint!!.measureText(badge.value.toString())
        }
        badge.setTextWidth(textWidth)
    }
}