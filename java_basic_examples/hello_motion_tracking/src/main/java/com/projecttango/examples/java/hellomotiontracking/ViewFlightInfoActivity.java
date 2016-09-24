package com.projecttango.examples.java.hellomotiontracking;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

    private static final String LOG_TAG = ViewFlightInfoActivity.class.getSimpleName();

    private static final String URL =
            "https://flifo-qa.api.aero/flifo/v3/flight/sin/ba/0012/d";

    OkHttpClient client = new OkHttpClient();

    Gson gson = new GsonBuilder().create();

    TextView flightInfoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_flight_info);
        flightInfoView = (TextView) findViewById(R.id.flight_info);

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

            Gson gson = new GsonBuilder().create();

            FlightApiResponse response = gson.fromJson(result, FlightApiResponse.class);

            EventBus.getDefault().post(response);

        } catch (IOException e) {
            throw new RuntimeException("Error getting flight info");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFlightInfoResult(FlightApiResponse event) {
        Log.d(LOG_TAG, "Result: " + event);
        flightInfoView.setText(event.toString());
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
