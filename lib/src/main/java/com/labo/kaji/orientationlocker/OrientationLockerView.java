package com.labo.kaji.orientationlocker;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * @author kakajika
 * @since 2015/07/11.
 */
public class OrientationLockerView extends RelativeLayout {

    public interface OrientationLockerViewListener {
        void onLockerRequestLock(@NonNull OrientationLockerView view, boolean lock);
    }

    private OrientationLocker mOrientationLocker;
    private OrientationLockerViewListener mListener;
    private boolean mLockerEnabled = true;
    private long mHideDelayOnLocked = 4000L;
    private long mHideDelayOnUnlocked = 3000L;

    public OrientationLockerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public OrientationLockerView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public OrientationLockerView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OrientationLockerView(@NonNull Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(@NonNull Context context) {
        if (isInEditMode()) {
            return;
        }
        mOrientationLocker = new OrientationLocker(context);
        mOrientationLocker.setOrientationChangedListener(new OrientationLocker.OrientationChangeListener() {
            @Override
            public void onOrientationChanged(int newOrientation) {
                handleOrientationChange(newOrientation);
            }
        });
        setVisibility(View.GONE);
    }

    /**
     * Add locker view to a window's center.
     * @param window
     */
    public void addToWindow(@NonNull Window window) {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.width = lp.height = 400;
        lp.gravity = Gravity.CENTER;
        addToWindow(window, lp);
    }

    /**
     * Add locker view to a window with custom LayoutParams.
     * @param window
     */
    public void addToWindow(@NonNull Window window, @NonNull FrameLayout.LayoutParams layoutParams) {
        ((ViewGroup) window.getDecorView()).addView(this, layoutParams);
    }

    /**
     * Get orientation handler.
     * @return
     */
    public OrientationLocker getOrientationLocker() {
        return mOrientationLocker;
    }

    /**
     * Set listener for orientation lock request.
     * @param listener
     */
    public void setOrientationLockerViewListener(@Nullable OrientationLockerViewListener listener) {
        mListener = listener;
    }

    /**
     * Enable or disable orientation handling and locking.
     * @param lockerEnabled
     */
    public void setLockerEnabled(boolean lockerEnabled) {
        mLockerEnabled = lockerEnabled;
        if (lockerEnabled) {
            mOrientationLocker.enable();
        } else {
            mOrientationLocker.disable();
            removeCallbacks(mHideScreenLockRunnable);
            setVisibility(View.GONE);
        }
    }

    /**
     * Set delay until view will be hidden.
     * @param delayOnLocked
     * @param delayOnUnlocked
     */
    public void setHideDelay(long delayOnLocked, long delayOnUnlocked) {
        mHideDelayOnLocked = delayOnLocked;
        mHideDelayOnUnlocked = delayOnUnlocked;
    }

    /**
     * Handle orientation event.
     * @param orientation
     */
    protected void handleOrientationChange(int orientation) {
        if (!mLockerEnabled || mOrientationLocker == null) {
            removeCallbacks(mHideScreenLockRunnable);
            setVisibility(View.GONE);
            return;
        }

        switch (orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
            case Configuration.ORIENTATION_LANDSCAPE:
                if (mOrientationLocker.isOrientationLocked()) {
                    int rotation = mOrientationLocker.getLockedRotation()- mOrientationLocker.getRotation()-(int)getRotation();
                    if (rotation >  180) rotation -= 360;
                    if (rotation < -180) rotation += 360;
                    animate()
                            .rotationBy(rotation)
                            .setInterpolator(new OvershootInterpolator())
                            .setDuration(400);
                    removeCallbacks(mHideScreenLockRunnable);
                } else {
                    setRotation(0);
                }
                break;
        }

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onLockerRequestLock(OrientationLockerView.this, !v.isSelected());
                    v.setSelected(mOrientationLocker.isOrientationLocked());
                }

                setVisibility(View.VISIBLE);
                setAlpha(1.0f);
                removeCallbacks(mHideScreenLockRunnable);
                postDelayed(mHideScreenLockRunnable, getHideDelay());
                if (!mOrientationLocker.isOrientationLocked()) {
                    int rotation = getRotation() > 180 ? 360 : 0;
                    animate()
                            .rotation(rotation)
                            .setInterpolator(new OvershootInterpolator())
                            .setDuration(400);
                }
            }
        });
        bringToFront();
        if (!isShown()) {
            setVisibility(View.VISIBLE);
            setAlpha(1.0f);
        }
        removeCallbacks(mHideScreenLockRunnable);
        postDelayed(mHideScreenLockRunnable, getHideDelay());
    }

    private long getHideDelay() {
        return isSelected() ? mHideDelayOnLocked : mHideDelayOnUnlocked;
    }

    private Runnable mHideScreenLockRunnable = new Runnable() {
        @Override
        public void run() {
            final float currentAlpha = getAlpha();
            ObjectAnimator animator = ObjectAnimator.ofFloat(this, "alpha", currentAlpha, 0.0f);
            animator.setDuration(300).addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator arg0) {
                    // Nothing to do.
                }
                @Override
                public void onAnimationRepeat(Animator arg0) {
                    // Nothing to do.
                }
                @Override
                public void onAnimationEnd(Animator arg0) {
                    setVisibility(View.GONE);
                }
                @Override
                public void onAnimationCancel(Animator arg0) {
                    // Nothing to do.
                }
            });
            animator.start();
        }
    };

}
