package name.nanek.changimon.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
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

import name.nanek.changimon.ChangimonApp;
import name.nanek.changimon.activity.DebugOverlayActivity;
import name.nanek.changimon.R;
import name.nanek.changimon.model.FlightRecordResponse;

public class FlightRecordOverlayService extends Service {

    private static final String LOG_TAG = FlightRecordOverlayService.class.getSimpleName();

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
		TextView flightInfoView = (TextView) overlayView.findViewById(R.id.overlay_text);

        FlightRecordResponse response = ChangimonApp.getInstance().currentResponse;
		if ( null != response ) {
            String displayString = response.getDisplayString();
            displayString += "\nMons Collected 0/2";

            Log.d(LOG_TAG, "displaying: " + displayString);
			flightInfoView.setText(displayString);
		} else {
            flightInfoView.setText("Enter Your Flight for Live Updates!");
        }


		//chatHead = new ImageView(this);
		//chatHead.setImageResource(R.drawable.face1);

		params= new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

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

        notification = new Notification(R.drawable.ic_launcher, "my Notification", System.currentTimeMillis());
		notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT | Notification.FLAG_ONLY_ALERT_ONCE;
		notification.contentIntent = notificationIntent();
		//notification.setLatestEventInfo(this, "my Notification", "my Notification", notificationIntent());
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(id, notification);
		return notification;
		*/
	}
	private PendingIntent notificationIntent() {
		Intent intent = new Intent(this, DebugOverlayActivity.class);
		PendingIntent pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return pending;
	}

	@Override
	public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
		super.onDestroy();
		if (overlayView != null)
			windowManager.removeView(overlayView);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}