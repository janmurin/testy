package sk.jmurin.android.testy.entities;

import android.os.Build;

/**
 * Created by jan.murin on 26-Aug-16.
 */
public class SkoreStats {

    public String username;
    public String deviceID;
    public int testID;
    public int skore;

    @Override
    public String toString() {
        return "SkoreStats{" +
                "username='" + username + '\'' +
                ", deviceID='" + deviceID + '\'' +
                ", testID=" + testID +
                ", skore=" + skore +
                '}';
    }


}
