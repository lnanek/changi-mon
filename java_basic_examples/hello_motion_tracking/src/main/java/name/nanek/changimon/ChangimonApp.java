package name.nanek.changimon;

import android.app.Application;

import name.nanek.changimon.model.FlightRecordResponse;

/**
 * Created by Lance on 9/24/16.
 */

public class ChangimonApp extends Application {

    private static ChangimonApp instance;

    public FlightRecordResponse currentResponse;

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }

    public static ChangimonApp getInstance() {
        return instance;
    }
}