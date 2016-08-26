package sk.jmurin.android.testy;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.util.Map;


/**
 * Global {@link Application} class.
 */
public class App extends Application {

    public static final String TAG = "Testy App";
    public static final String DEFAULT_USERNAME = "default";
    public static String USERNAME = DEFAULT_USERNAME;
    public static String DEVICE_ID;
    public static final String DEBUG = "debug";

    private static Context sInstance;


    @Override
    public void onCreate() {
        super.onCreate();
        //Log.d(TAG, "App created...");
        zaloguj(DEBUG, TAG, "App created...");
        sInstance = this;
    }

    public static void zaloguj(String level, String TAG, String message) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        if (level.equals(DEBUG)) {
            Log.d(TAG, "MYAPP_ " + message);
        }
    }

    public static Context getContext() {
        return sInstance;
    }
}
