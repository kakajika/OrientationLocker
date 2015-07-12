package com.labo.kaji.orientationlocker;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

import trikita.log.Log;

/**
 * @author kakajika
 * @since 2015/07/11.
 */
public class OrientationLocker extends OrientationEventListener {

    public interface OrientationChangeListener{
        void onOrientationChanged(int newOrientation);
    }

    private OrientationChangeListener mListener;
    private int mOrientation;
    private int mFixedRotation;
    private int mLockedRotation;
    private int mLockedOrientation;
    private boolean mOrientationLocked;

    private final Resources mResources;
    private final Display mDefaultDisplay;
    private int mNaturalOrientation = Configuration.ORIENTATION_PORTRAIT;
    private int mUnnaturalOrientation = Configuration.ORIENTATION_LANDSCAPE;

    public OrientationLocker(@NonNull Context context) {
        super(context);
        mResources = context.getResources();
        mDefaultDisplay = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }

    /**
     * Set listener for orientation change event.
     * @param listener
     */
    void setOrientationChangedListener(@Nullable OrientationChangeListener listener){
        mListener = listener;
    }

    /**
     * Get current orientation.
     * @return
     */
    public int getOrientation(){
        return mOrientation;
    }

    /**
     * Get current rotation degree.
     * @return
     */
    public int getRotation() {
        return mFixedRotation;
    }

    /**
     * Get whether orientation is locked.
     * @return
     */
    public boolean isOrientationLocked() {
        return mOrientationLocked;
    }

    /**
     * Get orientation of locked screen.
     * @return
     */
    public int getLockedOrientation() {
        return mLockedOrientation;
    }

    /**
     * Get rotation degree of locked screen.
     * @return
     */
    public int getLockedRotation() {
        return mLockedRotation;
    }

    /**
     * Lock orientation to current value.
     * @param activity
     */
    public void lockCurrentOrientation(@NonNull Activity activity) {
        setActivityOrientationLocked(activity, true);
        mOrientationLocked = true;
        mLockedOrientation = mOrientation;
        mLockedRotation = mFixedRotation;
    }

    /**
     * Unlock orientation change.
     * @param activity
     */
    public void unlockOrientation(@NonNull Activity activity) {
        setActivityOrientationLocked(activity, false);
        mOrientationLocked = false;
    }

    @Override
    public void onOrientationChanged(int rotation) {
        if (rotation == ORIENTATION_UNKNOWN) {
            return;
        }

        // initialize
        if (mOrientation == 0) {
            mOrientation = mResources.getConfiguration().orientation;
            switch (mDefaultDisplay.getRotation()) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                        mNaturalOrientation = Configuration.ORIENTATION_LANDSCAPE;
                        mUnnaturalOrientation = Configuration.ORIENTATION_PORTRAIT;
                    }
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                        mNaturalOrientation = Configuration.ORIENTATION_LANDSCAPE;
                        mUnnaturalOrientation = Configuration.ORIENTATION_PORTRAIT;
                    }
                    break;
            }
        }

        if (mOrientation == mUnnaturalOrientation) {
            if (Math.abs(rotation) <= 10 || Math.abs(rotation-360) <= 10) {
                mFixedRotation = 0;
                notifyOrientationChange(mNaturalOrientation);
            }else if (Math.abs(rotation-180) <= 10) {
                mFixedRotation = 180;
                notifyOrientationChange(mNaturalOrientation);
            }
        } else if (mOrientation == mNaturalOrientation) {
            if (Math.abs(rotation-90) <= 10) {
                mFixedRotation = 90;
                notifyOrientationChange(mUnnaturalOrientation);
            } else if (Math.abs(rotation-270) <= 10) {
                mFixedRotation = 270;
                notifyOrientationChange(mUnnaturalOrientation);
            }
        }
    }

    private void notifyOrientationChange(int orientation) {
        // When not locked, use orientation value of Configuration.
        if (!mOrientationLocked && mOrientation == mResources.getConfiguration().orientation) {
            return;
        }
        mOrientation = orientation;
        if (mListener != null) {
            mListener.onOrientationChanged(orientation);
        }
    }

    /**
     * Lock or unlock activity's orientation change.
     * @param locked
     */
    public static void setActivityOrientationLocked(Activity activity, boolean locked) {
        if (locked) {
            Configuration config = activity.getResources().getConfiguration();
            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Display display = ((WindowManager)activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                switch (display.getRotation()) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_90:
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        break;
                    case Surface.ROTATION_180:
                    case Surface.ROTATION_270:
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                        break;
                }
            } else {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

}
