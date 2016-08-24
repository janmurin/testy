package sk.jmurin.android.testy;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.util.Map;

import sk.jmurin.android.testy.entities.TestStats;

/**
 * Global {@link Application} class.
 */
public class App extends Application {

    public static final String TAG = "Testy App";
    public static final String DEFAULT_USERNAME = "default";
    public static String USERNAME = DEFAULT_USERNAME;


    private static Context sInstance;
    public static Map<String, TestStats> testStatsMap;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "App created...");
        sInstance = this;
        //PlanetsProviderHelper.insertPlanets(this);
    }

    public static Context getContext() {
        return sInstance;
    }
}
