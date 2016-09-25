package name.nanek.changimon.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import name.nanek.changimon.ChangimonApp;
import name.nanek.changimon.activity.DebugOverlayActivity;
import name.nanek.changimon.R;
import name.nanek.changimon.model.FlightRecordRequest;
import name.nanek.changimon.model.FlightRecordResponse;
import name.nanek.changimon.model.UpdateOverlayRequest;

public class FlightRecordOverlayService extends Service {

    private static final String LOG_TAG = FlightRecordOverlayService.class.getSimpleName();

    private static final int POLL_SITA_MS = 15 * 1000;

	private WindowManager windowManager;
	//private ImageView chatHead;
	private View overlayView;
	WindowManager.LayoutParams params;
	private Notification notification;
    int mNotificationId = 001;

	@Override
	public void onCreate() {
        Log.d(LOG_TAG, "onCreate");
		super.onCreate();

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		LayoutInflater inflator = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		overlayView = inflator.inflate(R.layout.service_flight_info_overlay, null);
        EventBus.getDefault().register(this);
        updateText();

        if (Build.VERSION.SDK_INT < 19) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_TOAST,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.x = 0;
		params.y = 100;
		
		//this code is for dragging the chat head
		overlayView.setOnTouchListener(new View.OnTouchListener() {
			private int initialX;
			private int initialY;
			private float initialTouchX;
			private float initialTouchY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					initialX = params.x;
					initialY = params.y;
					initialTouchX = event.getRawX();
					initialTouchY = event.getRawY();
					return true;
				case MotionEvent.ACTION_UP:
					return true;
				case MotionEvent.ACTION_MOVE:
					params.x = initialX
							+ (int) (event.getRawX() - initialTouchX);
					params.y = initialY
							+ (int) (event.getRawY() - initialTouchY);
					windowManager.updateViewLayout(overlayView, params);
					return true;
				}
				return false;
			}
		});
		windowManager.addView(overlayView, params);

		startForeground(mNotificationId, foregroundNotification());

        checkForSitaUpdate.run();
	}

    private Runnable checkForSitaUpdate = new Runnable() {
        @Override
        public void run() {
            if ( null == overlayView ) {
                return;
            }

            FlightRecordRequest request = ChangimonApp.getInstance().currentRequest;
            if ( null != request ) {
                // poll SITA for updates
            }
            overlayView.postDelayed(this, POLL_SITA_MS);
        }
    };

    public void updateText() {
        if ( null == overlayView ) {
            return;
        }
        TextView flightInfoView = (TextView) overlayView.findViewById(R.id.overlay_text);

        String displayString;
        FlightRecordResponse response = ChangimonApp.getInstance().currentResponse;
        if ( null != response ) {
            displayString = response.getDisplayString();
        } else {
            displayString = "Enter Your Flight for Live Updates!";
        }
        displayString += "\nMons Collected " + ChangimonApp.getInstance().collected + "/2";
        Log.d(LOG_TAG, "displaying: " + displayString);
        flightInfoView.setText(displayString);
    }

	protected Notification foregroundNotification()
	{

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Changi-mon Flight Info")
                        .setContentText("Click to Enable/Disable");

        Intent resultIntent = new Intent(this, DebugOverlayActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        int mNotificationId = 001;
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification = mBuilder.build();
        mNotifyMgr.notify(mNotificationId, notification);
        return notification;
        /*
		notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT | Notification.FLAG_ONLY_ALERT_ONCE;
		*/
	}

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateOverlayRequest(UpdateOverlayRequest event) {
        updateText();
    }

	@Override
	public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
        if (overlayView != null) {
            EventBus.getDefault().unregister(this);
            windowManager.removeView(overlayView);
            overlayView = null;
        }
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}