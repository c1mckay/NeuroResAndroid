package edu.sdsc.neurores.helper;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by tbpetersen on 4/30/2018.
 */

public class FormatHelper {
    public static SimpleDateFormat getDatabaseDateFormatter() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter;
    }

    public static SimpleDateFormat getLocalDateFormatter() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());
        return formatter;
    }
}
