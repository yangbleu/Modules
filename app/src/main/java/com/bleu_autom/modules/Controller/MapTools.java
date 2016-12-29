package com.bleu_autom.modules.Controller;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

/**
 * Created by GeorgeYang on 2016/12/15.
 */

public class MapTools {
    public static void naviToLatlng(Activity activity, double latitude, double longitude, final boolean isNavi) {
        MapTools.naviToLatlng(activity, String.valueOf(latitude), String.valueOf(longitude), isNavi);
    }

    public static void naviToLatlng(Activity activity, String lat, String lng, final boolean isNavi){
        if (lat.isEmpty()||lat.isEmpty()) {
            Toast.makeText(activity, "地址為空值", Toast.LENGTH_SHORT).show();
            return;
        }
        String addrMapURI;
        if (isNavi){
            Toast.makeText(activity, "導航到 "+lat+":"+lng, Toast.LENGTH_SHORT).show();
            addrMapURI = String.format("google.navigation:q=%s,%s",lat,lng);
        } else {
            Toast.makeText(activity, "開啟地圖到 "+lat+":"+lng, Toast.LENGTH_SHORT).show();
            addrMapURI = String.format("geo:%s,%s",lat,lng);
        }
        Uri gmmIntentUri = Uri.parse(addrMapURI);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        activity.startActivity(mapIntent);
    }
}
