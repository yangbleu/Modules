package com.bleu_autom.modules.Controller;

import android.content.Context;
import android.content.SharedPreferences;

public class DataValue { //Sharperfence

    public static final int REQUEST_ENABLE_BT = 1601;
    public static final int REQUEST_PLACE_PICKER = 1602;
    public static final int REQUEST_LAST_LOCATION = 1603;
    public static final int REQUEST_LOCATION_UPDATES = 1604;
    public static final int REQUEST_GOOGLE_SIGN_IN = 1605;
    public static final int REQUEST_FB_SIGN_IN = 64206;//facebook fixed 64206

    public static int GOOGLE_API_CLIENT_ID = 0;

    /**
     * Constant used in the location settings dialog.
     */
    public static final int REQUEST_CHECK_LOCATION_RESOLUTION_SETTINGS = 0x1;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 30000;
    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 3;

    //save in local_data
    public static final String reserve_space_list ="reserve_list";
    public static final String floor_sensor_data ="floor_sensor_data";
    public static final String space_data ="space_data";

    public static final String reserve_list ="reserve_list";
    public static final String reserve_data ="reserve_data";


    public static void saveData(Context context, String key, String value) {
        SharedPreferences.Editor SPE = context.getApplicationContext().getSharedPreferences("data_save", context.MODE_PRIVATE).edit();
        SPE.putString(key, value).apply();
    }
    public static String getData(Context context, String value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences("data_save", context.MODE_ENABLE_WRITE_AHEAD_LOGGING);
        return settings.getString(value, "");
    }
}



