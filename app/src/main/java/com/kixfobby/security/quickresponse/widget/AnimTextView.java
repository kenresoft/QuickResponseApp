package com.kixfobby.security.quickresponse.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.IntDef;
import androidx.appcompat.widget.AppCompatTextView;

import com.kixfobby.security.quickresponse.R;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;


public class AnimTextView extends AppCompatTextView {
    public static final int DEFAULT_TIME_OUT = 15000;
    public static final int MILLISECONDS = 1,
            SECONDS = 2,
            MINUTES = 3;
    int duration;
    boolean animate, isShown, stopped;
    private CharSequence[] texts;
    private Animation inAnimation, outAnimation;
    private Handler handler;
    private int position = 0;


    public AnimTextView(Context context) {
        super(context);
        init();
    }

    public AnimTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAttributes(attrs);
        init();
    }

    public AnimTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttributes(attrs);
        init();
    }


    public void play() {
        isShown = true;
        startAnimation();
    }

    /**
     * Pauses the animation
     * Should only be used if you notice {@link #onDetachedFromWindow()} is not being executed as expected
     */
    public void pause() {
        isShown = false;
        stopAnimation();
    }

    public void stop() {
        isShown = false;
        stopped = true;
        stopAnimation();
    }

    public void restart() {
        if (!stopped) {
            stop();
        }
        stopped = false;
        isShown = true;
        startAnimation();
    }


    public void refresh() {
        stopAnimation();
        startAnimation();
    }

    private void init() {
        inAnimation = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        outAnimation = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        handler = new Handler();
    }

    private void getAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.AnimTextView);
        this.texts = typedArray.getTextArray(R.styleable.AnimTextView_texts);
        this.duration = typedArray.getInteger(R.styleable.AnimTextView_duration, 2000);
        this.animate = typedArray.getBoolean(R.styleable.AnimTextView_animate, true);
        typedArray.recycle();
    }


    public void setTexts(@NotNull String[] texts) {
        if (texts.length < 1) {
            throw new ArrayIndexOutOfBoundsException("Array must be of size greater then 0");
        } else {
            this.texts = texts;
        }
    }

    public void setTexts(@NotNull ArrayList<String> texts) {
        if (texts.isEmpty()) {
            throw new ArrayIndexOutOfBoundsException("Array must be of size greater then 0");
        } else {
            this.texts = texts.toArray(new CharSequence[texts.size()]);
        }
    }

    public void setTexts(int resId) {
        String[] data = getContext().getResources().getStringArray(resId);
        if (data.length < 1) {
            throw new ArrayIndexOutOfBoundsException("Array must be of size greater then 0");
        } else {
            this.texts = data;
        }
    }

    public void setInAnimation(Animation animation) {
        this.inAnimation = animation;
    }

    public void setOutAnimation(Animation animation) {
        this.outAnimation = animation;
    }

    public void setAnimate(boolean animate) {
        this.animate = animate;
    }

    public boolean isAnimated() {
        return this.animate;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setDuration(int duration, @TimeUnit int timeUnit) {
        int multiplier;
        switch (timeUnit) {
            case SECONDS:
                multiplier = 1000;
                break;
            case MINUTES:
                multiplier = 60000;
                break;
            case MILLISECONDS:
            default:
                multiplier = 1;
        }
        this.duration = duration * multiplier;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        pause();
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        play();
    }


    @Override
    public void startAnimation(Animation animation) {
        if (isShown && !stopped) {
            super.startAnimation(animation);
        }
    }

    protected void startAnimation() {
        if (!isInEditMode()) {
            setText(texts[position]);
            if (animate) {
                startAnimation(inAnimation);
            }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (animate) {
                        startAnimation(outAnimation);

                        if (getAnimation() != null) {
                            getAnimation().setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    if (isShown) {
                                        position = position == texts.length - 1 ? 0 : position + 1;
                                        startAnimation();
                                    }
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                        }
                    } else {
                        position = position == texts.length - 1 ? 0 : position + 1;
                        startAnimation();
                    }
                }
            }, duration);
        }
    }

    private void stopAnimation() {
        handler.removeCallbacksAndMessages(null);
        if (getAnimation() != null) getAnimation().cancel();
    }


    @IntDef({MILLISECONDS, SECONDS, MINUTES})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TimeUnit {
    }
}
