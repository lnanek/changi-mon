/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package name.nanek.changimon.activity;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.Tango.OnTangoUpdateListener;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import name.nanek.changimon.ChangimonApp;
import name.nanek.changimon.R;
import name.nanek.changimon.model.UpdateOverlayRequest;

/**
 * Main Activity class for the Motion Tracking API Sample. Handles the connection to the Tango
 * service and propagation of Tango pose data Layout view.
 */
public class ViewAirportMapActivity extends Activity {

    private static final String TAG = ViewAirportMapActivity.class.getSimpleName();

    public static float sTangoToPixelFactor = 220;

    private static final Boolean DEBUG_AUTO_X_MOVEMENT = null;
    //private static final Boolean DEBUG_AUTO_X_MOVEMENT = false;

    private static final int HUMAN_TOKEN_START_PERCENT_X = 50;
    private static final int HUMAN_TOKEN_START_PERCENT_Y = 85;

    private Tango mTango;
    private TangoConfig mConfig;
    private View mHumanTokenView;
    private View mMarker1View;
    private View mMarker2View;
    private ViewGroup mContentView;

    private Float humanTokenCurrentX;
    private Float humanTokenCurrentY;
    private int humanTokenRenderX;
    private int humanTokenRenderY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_airport_map);
        mHumanTokenView = findViewById(R.id.human_token);
        mMarker1View = findViewById(R.id.marker_1);
        mMarker2View = findViewById(R.id.marker_2);
        mContentView = (ViewGroup) findViewById(R.id.content_view);

        ViewTreeObserver vto = mContentView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // do something now when the object is loaded
                // e.g. find the real size of it etc
                mContentView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                setHumanTokenInCenter();
            }
        });
    }

    private synchronized void setHumanTokenInCenter() {
        final int contentWidth = mContentView.getWidth();
        final int contentHeight = mContentView.getHeight();

        final int tokenWidth = mHumanTokenView.getWidth();
        final int tokenHeight = mHumanTokenView.getHeight();

        humanTokenCurrentX = (contentWidth * HUMAN_TOKEN_START_PERCENT_X / 100.0f) - tokenWidth;
        humanTokenCurrentY = (contentHeight * HUMAN_TOKEN_START_PERCENT_Y / 100.0f) - tokenHeight;

        humanTokenRenderX = Math.round(humanTokenCurrentX);
        humanTokenRenderY = Math.round(humanTokenCurrentY);

        Log.d(TAG, "Updating human token margins to: " + humanTokenRenderX + ", " + humanTokenRenderY);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mHumanTokenView.getLayoutParams();
        params.setMargins(humanTokenRenderX, humanTokenRenderY, 0, 0);
        mHumanTokenView.requestLayout();

        mHumanTokenView.setVisibility(View.VISIBLE);
        //mHumanTokenView.setLayoutParams(params);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            // Initialize Tango Service as a normal Android Service, since we call mTango.disconnect()
            // in onPause, this will unbind Tango Service, so every time when onResume gets called, we
            // should create a new Tango object.
            mTango = new Tango(ViewAirportMapActivity.this, new Runnable() {
                // Pass in a Runnable to be called from UI thread when Tango is ready, this Runnable
                // will be running on a new thread.
                // When Tango is ready, we can call Tango functions safely here only when there is no UI
                // thread changes involved.
                @Override
                public void run() {
                    synchronized (ViewAirportMapActivity.this) {
                        mConfig = setupTangoConfig(mTango);

                        try {
                            setTangoListeners();
                        } catch (TangoErrorException e) {
                            Log.e(TAG, getString(R.string.exception_tango_error), e);
                        } catch (SecurityException e) {
                            Log.e(TAG, getString(R.string.permission_motion_tracking), e);
                        }
                        try {
                            mTango.connect(mConfig);
                        } catch (TangoOutOfDateException e) {
                            Log.e(TAG, getString(R.string.exception_out_of_date), e);
                        } catch (TangoErrorException e) {
                            Log.e(TAG, getString(R.string.exception_tango_error), e);
                        }
                    }
                }
            });
        } catch(UnsatisfiedLinkError e) {
            Log.d(TAG, "Ignore not Tango, just display map");
        }

        autoMovement.run();
    }

    private Runnable autoMovement = new Runnable() {
        @Override
        public void run() {
            if ( null == DEBUG_AUTO_X_MOVEMENT ) {
                return;
            }

            final float autoX = DEBUG_AUTO_X_MOVEMENT ? 0.01f : -0.01f;

            updateHumanTokenPosition(autoX, 0, 0);

            mHumanTokenView.postDelayed(this, 200);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        synchronized (this) {
            if ( null != mTango ) {
                try {
                    mTango.disconnect();
                } catch (TangoErrorException e) {
                    Log.e(TAG, getString(R.string.exception_tango_error), e);
                }
            }
        }
    }

    /**
     * Sets up the tango configuration object. Make sure mTango object is initialized before
     * making this call.
     */
    private TangoConfig setupTangoConfig(Tango tango) {
        // Create a new Tango Configuration and enable the ViewAirportMapActivity API.
        TangoConfig config = new TangoConfig();
        config = tango.getConfig(config.CONFIG_TYPE_DEFAULT);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING, true);

        // Tango service should automatically attempt to recover when it enters an invalid state.
        config.putBoolean(TangoConfig.KEY_BOOLEAN_AUTORECOVERY, true);
        return config;
    }

    /**
     * Set up the callback listeners for the Tango service, then begin using the Motion
     * Tracking API. This is called in response to the user clicking the 'Start' Button.
     */
    private void setTangoListeners() {
        // Lock configuration and connect to Tango
        // Select coordinate frame pair
        final ArrayList<TangoCoordinateFramePair> framePairs =
                new ArrayList<TangoCoordinateFramePair>();
        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                TangoPoseData.COORDINATE_FRAME_DEVICE));

        // Listen for new Tango data
        mTango.connectListener(framePairs, new OnTangoUpdateListener() {
            @Override
            public void onPoseAvailable(final TangoPoseData pose) {
                onPoseUpdate(pose);
            }

            @Override
            public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
                // We are not using onXyzIjAvailable for this app.
            }

            @Override
            public void onPointCloudAvailable(TangoPointCloudData pointCloud) {
                // We are not using onPointCloudAvailable for this app.
            }

            @Override
            public void onTangoEvent(final TangoEvent event) {
                // Ignoring TangoEvents.
            }

            @Override
            public void onFrameAvailable(int cameraId) {
                // We are not using onFrameAvailable for this application.
            }
        });
    }

    private float lastPositionX;
    private float lastPositionY;
    private float lastPositionZ;

    /**
     * Log the Position and Orientation of the given pose in the Logcat as information.
     *
     * @param pose the pose to log.
     */
    private void onPoseUpdate(TangoPoseData pose) {
        StringBuilder stringBuilder = new StringBuilder();

        float translation[] = pose.getTranslationAsFloats();

        float newPositionX = translation[0];
        float newPositionY = translation[1];
        float newPositionZ = translation[2];

        float deltaX = newPositionX - lastPositionX;
        float deltaY = newPositionY - lastPositionY;
        float deltaZ = newPositionZ - lastPositionZ;

        lastPositionX = newPositionX;
        lastPositionY = newPositionY;
        lastPositionZ = newPositionZ;

        stringBuilder.append("Position: " +
                lastPositionX + ", " + lastPositionY + ", " + lastPositionZ);

        float orientation[] = pose.getRotationAsFloats();
        stringBuilder.append(". Orientation: " +
                orientation[0] + ", " + orientation[1] + ", " +
                orientation[2] + ", " + orientation[3]);

        Log.i(TAG, stringBuilder.toString());

        updateHumanTokenPosition(deltaX, deltaY, deltaZ);
    }

    private synchronized  void updateHumanTokenPosition(float deltaX, float deltaY, float deltaZ) {
        Log.d(TAG, "updateHumanTokenPosition(" + deltaX + ", " + deltaY + ", " + deltaZ + ")");

        // No layout yet, ignore
        if ( null == humanTokenCurrentX || null == humanTokenCurrentY ) {
            return;
        }

        humanTokenCurrentX += (deltaX * sTangoToPixelFactor);
        humanTokenCurrentY += (deltaY * sTangoToPixelFactor);

        int newHumanTokenRenderX = Math.round(humanTokenCurrentX);
        int newHumanTokenRenderY = Math.round(humanTokenCurrentY);

        // Abort if didn't move
        if (humanTokenRenderX == newHumanTokenRenderX &&
                humanTokenRenderY == newHumanTokenRenderY) {
            return;
        }

        humanTokenRenderX = newHumanTokenRenderX;
        humanTokenRenderY = newHumanTokenRenderY;

        Log.d(TAG, "Updating human token margins to: " + humanTokenRenderX + ", " + humanTokenRenderY);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                checkCollected();
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mHumanTokenView.getLayoutParams();
                params.setMargins(humanTokenRenderX, humanTokenRenderY, 0, 0);
                mHumanTokenView.requestLayout();
            }
        });
    }

    private void checkCollected() {
        Log.d(TAG, "checkCollected");

        if (mMarker1View.getVisibility() == View.VISIBLE && checkCollision(mMarker1View)) {
            mMarker1View.setVisibility(View.INVISIBLE);
            ChangimonApp.getInstance().collected++;
            EventBus.getDefault().post(new UpdateOverlayRequest());
        }

        if (mMarker2View.getVisibility() == View.VISIBLE && checkCollision(mMarker2View)) {
            mMarker2View.setVisibility(View.INVISIBLE);
            ChangimonApp.getInstance().collected++;
            EventBus.getDefault().post(new UpdateOverlayRequest());
        }
    }

    private boolean checkCollision(View target) {

        ViewGroup.MarginLayoutParams targetParams = (ViewGroup.MarginLayoutParams) target.getLayoutParams();
        final int targetLeft = targetParams.leftMargin;
        final int targetRight = targetLeft + target.getWidth();
        final int targetTop = targetParams.topMargin;
        final int targetBottom = targetTop + target.getHeight();
        Log.d(TAG, "checkCollision target at [" + targetLeft + ", " + targetTop + ", " +
            targetRight + ", " + targetBottom + "]");

        final int humanTokenRenderRight = humanTokenRenderX + mHumanTokenView.getWidth();
        final int humanTokenRenderBottom = humanTokenRenderY + mHumanTokenView.getHeight();
        Log.d(TAG, "checkCollision human at [" + humanTokenRenderX + ", " + humanTokenRenderY + ", " +
                humanTokenRenderRight + ", " + humanTokenRenderBottom + "]");

        boolean leftEdgeInside = humanTokenRenderX > targetLeft && humanTokenRenderX < targetRight;
        boolean rightEdgeInside = humanTokenRenderRight > targetLeft && humanTokenRenderRight < targetRight;
        if (leftEdgeInside || rightEdgeInside) {
            boolean topInside = humanTokenRenderY > targetTop && humanTokenRenderY < targetBottom;
            boolean bottomInside = humanTokenRenderBottom > targetTop && humanTokenRenderBottom < targetBottom;
            if (topInside || bottomInside) {
                Log.d(TAG, "checkCollision leftEdgeInside = " + leftEdgeInside);
                Log.d(TAG, "checkCollision rightEdgeInside = " + rightEdgeInside);
                Log.d(TAG, "checkCollision topInside = " + topInside);
                Log.d(TAG, "checkCollision bottomInside = " + bottomInside);
                return true;
            }
        }

        return false;
    }
}
