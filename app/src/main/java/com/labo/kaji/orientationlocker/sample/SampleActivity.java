package com.labo.kaji.orientationlocker.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.labo.kaji.orientationlocker.OrientationLockerView;

public class SampleActivity extends AppCompatActivity {

    private OrientationLockerView mLockerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample);

        // Create view and add to window.
        mLockerView = (OrientationLockerView)View.inflate(this, R.layout.orientation_locker, null);
        mLockerView.addToWindow(getWindow());
        mLockerView.setOrientationLockerViewListener(new OrientationLockerView.OrientationLockerViewListener() {
            @Override
            public void onLockerRequestLock(OrientationLockerView view, boolean lock) {
                if (lock) {
                    view.getOrientationLocker().lockCurrentOrientation(SampleActivity.this);
                } else {
                    view.getOrientationLocker().unlockOrientation(SampleActivity.this);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start handling screen rotation.
        mLockerView.setLockerEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop handling screen rotation.
        mLockerView.setLockerEnabled(false);
    }

}
