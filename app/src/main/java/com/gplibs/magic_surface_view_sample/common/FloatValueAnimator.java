package com.gplibs.magic_surface_view_sample.common;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * 0~1 或者 1~0 的ValueAnimator
 */
public class FloatValueAnimator implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

    public interface FloatValueAnimatorListener {
        void onAnimationUpdate(float value);
        void onStop();
    }

    private final int STATE_NONE = 0;
    private final int STATE_RUNNING = 1;
    private final int STATE_STOPPED = 2;

    private int mState = STATE_NONE;
    private boolean mStopping = false;
    private ValueAnimator mAnimator;
    private int mDuration = 600;
    private TimeInterpolator mInterpolator;
    private List<FloatValueAnimatorListener> mListeners = new ArrayList<>(2);

    public FloatValueAnimator(int duration) {
        mDuration = duration;
    }

    public void addListener(FloatValueAnimatorListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeListener(FloatValueAnimatorListener listener) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public void setInterpolator(TimeInterpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        onAnimValueChanged((float) animation.getAnimatedValue());
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        mState = STATE_STOPPED;
        mAnimator = null;
        onStop();
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        mState = STATE_STOPPED;
        mAnimator = null;
        onStop();
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }

    public void reset() {
        mState = STATE_NONE;
    }

    public boolean isStopped() {
        return mState == STATE_STOPPED;
    }

    public boolean isRunning() {
        return mState == STATE_RUNNING;
    }

    public synchronized void start(final boolean isReverse) {
        if (mState == STATE_RUNNING) {
            return;
        }
        mState = STATE_RUNNING;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (isReverse) {
                    mAnimator = ValueAnimator.ofFloat(1, 0);
                } else {
                    mAnimator = ValueAnimator.ofFloat(0, 1);
                }
                if (mInterpolator != null) {
                    mAnimator.setInterpolator(mInterpolator);
                }
                mAnimator.addUpdateListener(FloatValueAnimator.this);
                mAnimator.addListener(FloatValueAnimator.this);
                mAnimator.setRepeatCount(0);
                mAnimator.setDuration(mDuration);
                mAnimator.start();
            }
        });
    }

    public synchronized void stop() {
        if (mAnimator == null) {
            return;
        }
        if (mStopping) {
            return;
        }
        mStopping = true;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mAnimator != null) {
                    mAnimator.cancel();
                    mAnimator = null;
                }
                mStopping = false;
            }
        });
    }

    private void onAnimValueChanged(float animValue) {
        for(int i = 0; i < mListeners.size(); ++i) {
            if (mListeners.get(i) != null) {
                mListeners.get(i).onAnimationUpdate(animValue);
            }
        }
    }

    private void onStop() {
        for(int i = 0; i < mListeners.size(); ++i) {
            if (mListeners.get(i) != null) {
                mListeners.get(i).onStop();
            }
        }
    }
}
