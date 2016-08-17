package sk.jmurin.android.testy.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

/**
 * Useful static methods for network operations.
 */
@SuppressWarnings("unused")
public final class NetworkUtils {

    /**
     * Returns details about the currently active default data network.
     *
     * @param context Application or Activity context
     * @return a NetworkInfo object for the current default network
     * or null if no network default network is currently active
     */
    public static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * Checks if device has active network connection.
     *
     * @param context Application or Activity context
     * @return true if device has internet connection or false otherwise
     */
    public static boolean isConnected(Context context) {
        NetworkInfo info = getActiveNetworkInfo(context);
        return info != null && info.isConnected();
    }

    /**
     * Checks if device has active network connection.
     *
     * @param info the NetworkInfo object
     * @return true if device has internet connection or false otherwise
     */
    public static boolean isConnected(NetworkInfo info) {
        return info != null && info.isConnected();
    }

    /**
     * Method to get WiFi status
     *
     * @return true if WiFi is enabled else false
     */
    public boolean isWiFiEnabled(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    /**
     * Checks if device is connected to wifi network.
     *
     * @param context Application or Activity context
     * @return true if device is connected to wifi or false otherwise
     */
    public static boolean isConnectedToWifi(Context context) {
        NetworkInfo info = getActiveNetworkInfo(context);
        return isConnected(info) && info.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * Checks if device is connected to mobile network.
     *
     * @param context context
     * @return true if device is connected to mobile network or false otherwise
     */
    public static boolean isConnectedToMobile(Context context) {
        NetworkInfo info = getActiveNetworkInfo(context);
        return isConnected(info) && info.getType() == ConnectivityManager.TYPE_MOBILE;
    }
}
