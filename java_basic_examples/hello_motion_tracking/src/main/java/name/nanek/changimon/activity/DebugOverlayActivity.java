package name.nanek.changimon.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import name.nanek.changimon.R;
import name.nanek.changimon.service.FlightRecordOverlayService;

public class DebugOverlayActivity extends Activity {

	private static final String LOG_TAG = DebugOverlayActivity.class.getSimpleName();

	Button startService;

	Button stopService;

	EditText cheatFactorEditText;

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

		cheatFactorEditText = (EditText) findViewById(R.id.cheat_factor_edit_text);

		final Button updateCheatFactorButton = (Button) findViewById(R.id.update_cheat_factor_button);
		updateCheatFactorButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				updateCheatFactor();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		cheatFactorEditText.setText("" + ViewAirportMapActivity.sTangoToPixelFactor);
	}

	private void updateCheatFactor() {
		final String cheatFactor = cheatFactorEditText.getText().toString();
		final float newCheatFactor = Float.parseFloat(cheatFactor);
		ViewAirportMapActivity.sTangoToPixelFactor = newCheatFactor;
		Log.d(LOG_TAG, "updateCheatFactor to " + newCheatFactor);
	}
}
