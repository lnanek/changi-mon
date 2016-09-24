package name.nanek.changimon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import name.nanek.changimon.service.FlightRecordOverlayService;

public class DebugOverlayActivity extends Activity {
	Button startService,stopService;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug_overlay);
		startService=(Button)findViewById(R.id.startService);
		stopService=(Button)findViewById(R.id.stopService);
		startService.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startService(new Intent(getApplication(), FlightRecordOverlayService.class));
				
			}
		});
		stopService.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				stopService(new Intent(getApplication(), FlightRecordOverlayService.class));
				
			}
		});
	}
}
