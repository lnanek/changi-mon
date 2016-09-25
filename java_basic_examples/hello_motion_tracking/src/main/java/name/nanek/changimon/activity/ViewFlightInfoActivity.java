package name.nanek.changimon.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import name.nanek.changimon.ChangimonApp;
import name.nanek.changimon.R;
import name.nanek.changimon.model.FlightRecord;
import name.nanek.changimon.model.FlightRecordRequest;
import name.nanek.changimon.model.FlightRecordResponse;
import name.nanek.changimon.service.FlightRecordOverlayService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Lance on 9/24/16.
 */

public class ViewFlightInfoActivity extends Activity {

    private static final String LOG_TAG = ViewFlightInfoActivity.class.getSimpleName();

    OkHttpClient client = new OkHttpClient();

    Gson gson = new GsonBuilder().create();

    EditText airlineInput;

    EditText flightNumberInput;

    TextView flightInfoView;

    Button viewFlightInfoButton;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_flight_info);
        progressBar = (ProgressBar) findViewById(R.id.ctrlActivityIndicator);
        flightInfoView = (TextView) findViewById(R.id.flight_info);
        airlineInput = (EditText) findViewById(R.id.airline_edit_text);
        flightNumberInput = (EditText) findViewById(R.id.flight_number_edit_text);
        viewFlightInfoButton = (Button) findViewById(R.id.view_flight_info_button);
        viewFlightInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewFlightInfoClicked();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    public void onViewFlightInfoClicked() {

        viewFlightInfoButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        final FlightRecordRequest request = new FlightRecordRequest();
        request.airline = airlineInput.getText().toString();
        request.flightNumber = flightNumberInput.getText().toString();
        EventBus.getDefault().post(request);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onFlightInfoRequest(FlightRecordRequest event) {
        try {
            final String url = event.generateUrl();
            Log.d(LOG_TAG, "Accessing URL: " + url);
            final String result = run(url);

            Gson gson = new GsonBuilder().create();

            FlightRecordResponse response = gson.fromJson(result, FlightRecordResponse.class);

            EventBus.getDefault().post(response);

        } catch (IOException e) {
            throw new RuntimeException("Error getting flight info");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFlightInfoResult(FlightRecordResponse event) {
        Log.d(LOG_TAG, "Result: " + event);

        ChangimonApp.getInstance().currentResponse = event;

        viewFlightInfoButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);

        //flightInfoView.setText(event.getDisplayString());

        startService(new Intent(getApplication(), FlightRecordOverlayService.class));

        final Intent startActivity = new Intent(this, ViewAirportMapActivity.class);
        startActivity(startActivity);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .header("Accept", "application/json")
                .header("X-apiKey", "2cfd0827f82ceaccae7882938b4b1627")
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

}
