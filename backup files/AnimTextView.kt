package com.kixfobby.security.quickresponse.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.IntDef
import androidx.appcompat.widget.AppCompatTextView
import com.kixfobby.security.quickresponse.R
import java.util.*

class AnimTextView : AppCompatTextView {
    var duration = 0
    var isAnimated = false
    var stopped = false
    private lateinit var texts: Array<CharSequence>
    //private val backgrounds: Array<Drawable>
    private var inAnimation: Animation? = null
    private var outAnimation: Animation? = null
    private var position = 0

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        getAttributes(attrs)
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        getAttributes(attrs)
        init()
    }

    fun play() {
        Companion.isShown = true
        startAnimation()
    }

    /**
     * Pauses the animation
     * Should only be used if you notice [.onDetachedFromWindow] is not being executed as expected
     */
    fun pause() {
        Companion.isShown = false
        stopAnimation()
    }

    fun stop() {
        Companion.isShown = false
        stopped = true
        stopAnimation()
    }

    fun restart() {
        if (!stopped) {
            stop()
        }
        stopped = false
        Companion.isShown = true
        startAnimation()
    }

    fun refresh() {
        stopAnimation()
        startAnimation()
    }

    private fun init() {
        inAnimation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
        outAnimation = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
        Companion.handler = Handler()
    }

    private fun getAttributes(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AnimTextView)
        texts = typedArray.getTextArray(R.styleable.AnimTextView_texts)
        //this.backgrounds = typedArray.getTextArray(R.styleable.AnimTextView_backgrounds);    Not possible yet
        duration = typedArray.getInteger(R.styleable.AnimTextView_duration, 2000)
        isAnimated = typedArray.getBoolean(R.styleable.AnimTextView_animate, true)
        typedArray.recycle()
    }

    fun setTexts(texts: Array<String>) {
        if (texts.size < 1) {
            throw ArrayIndexOutOfBoundsException("Array must be of size greater then 0")
        } else {
            this.texts =  texts as Array<CharSequence>
        }
    }

    fun setTexts(texts: ArrayList<String>, backgrounds: ArrayList<Drawable?>?) {
        if (texts.isEmpty()) {
            throw ArrayIndexOutOfBoundsException("Array must be of size greater then 0")
        } /*else if (backgrounds.isEmpty()) {
            throw new ArrayIndexOutOfBoundsException("Image Array must be of size greater then 0");
        }*/ else {
            this.texts = texts.toTypedArray()
            //this.backgrounds = backgrounds.toArray(new Drawable[backgrounds.size()]);
        }
    }

    fun setTexts(resId: Int) {
        val data = context.resources.getStringArray(resId)
        if (data.size < 1) {
            throw ArrayIndexOutOfBoundsException("Array must be of size greater then 0")
        } else {
            texts = data as Array<CharSequence>
        }
    }

    fun setInAnimation(animation: Animation?) {
        inAnimation = animation
    }

    fun setOutAnimation(animation: Animation?) {
        outAnimation = animation
    }

    fun setAnimate(animate: Boolean) {
        isAnimated = animate
    }

    @JvmName("setDuration1")
    fun setDuration(duration: Int) {
        this.duration = duration
    }

    fun setDuration(duration: Int, @TimeUnit timeUnit: Int) {
        val multiplier: Int
        multiplier = when (timeUnit) {
            SECONDS -> 1000
            MINUTES -> 60000
            MILLISECONDS -> 1
            else -> 1
        }
        this.duration = duration * multiplier
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pause()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        play()
    }

    override fun startAnimation(animation: Animation) {
        if (Companion.isShown && !stopped) {
            super.startAnimation(animation)
        }
    }

    protected fun startAnimation() {
        if (!isInEditMode) {
            text = texts[position]
            //background = backgrounds[position]
            if (isAnimated) {
                startAnimation(inAnimation!!)
            }
            Companion.handler!!.postDelayed(object : Runnable {
                override fun run() {
                    if (isAnimated) {
                        startAnimation(outAnimation!!)
                        if (animation != null) {
                            animation.setAnimationListener(object : Animation.AnimationListener {
                                override fun onAnimationStart(animation: Animation) {}
                                override fun onAnimationEnd(animation: Animation) {
                                    if (Companion.isShown) {
                                        position = if (position == texts.size - 1) 0 else position + 1
                                        startAnimation()
                                    }
                                }

                                override fun onAnimationRepeat(animation: Animation) {}
                            })
                        }
                    } else {
                        position = if (position == texts.size - 1) 0 else position + 1
                        startAnimation()
                    }
                }
            }, duration.toLong())
        }
    }

    private fun stopAnimation() {
        Companion.handler!!.removeCallbacksAndMessages(null)
        if (animation != null) animation.cancel()
    }

    @IntDef(MILLISECONDS, SECONDS, MINUTES)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class TimeUnit

    companion object {
        const val DEFAULT_TIME_OUT = 15000
        const val MILLISECONDS = 1
        const val SECONDS = 2
        const val MINUTES = 3
        var isShown = false
        private lateinit var handler: Handler
    }
}