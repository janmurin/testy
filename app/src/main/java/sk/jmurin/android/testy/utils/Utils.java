package sk.jmurin.android.testy.utils;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by jan.murin on 17-Aug-16.
 */
public class Utils {

    public static String getStringUTFFromInputStream(InputStream is) {
        Scanner scanner = new Scanner(is, "utf-8");
        StringBuilder sb = new StringBuilder();
        while (scanner.hasNextLine()) {
            sb.append(scanner.nextLine());
        }
        return sb.toString();
    }
}
