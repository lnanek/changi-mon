package com.projecttango.examples.java.hellomotiontracking;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Lance on 9/24/16.
 */

public class ViewFlightInfoActivity extends Activity {

    public class FlightInfoRequest {
        String airline;
        String flightNumber;
    }

    public class FlightInfoResult {
        String result;
    }

    private static final String LOG_TAG = ViewFlightInfoActivity.class.getSimpleName();

    private static final String URL =
            "https://flifo-qa.api.aero/flifo/v3/flight/sin/ba/0012/d";

    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_flight_info);

    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);

        EventBus.getDefault().post(new FlightInfoRequest());
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onFlightInfoRequest(FlightInfoRequest event) {
        try {
            final String result = run(URL);

            final FlightInfoResult resultEvent = new FlightInfoResult();
            resultEvent.result = result;
            EventBus.getDefault().post(resultEvent);

        } catch (IOException e) {
            throw new RuntimeException("Error getting flight info");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFlightInfoResult(FlightInfoResult event) {
        Log.d(LOG_TAG, "Result: " + event.result);
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
