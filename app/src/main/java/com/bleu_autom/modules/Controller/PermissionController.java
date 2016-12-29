package com.bleu_autom.modules.Controller;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by GeorgeYang on 2016/12/21.
 */

public class PermissionController {
    private static final String LOG_TAG = "PermissionController";
    /**
     * Permission RequestCode
     */
    private static final String MSG_PERMISSION_CONFIRM = "權限確認";
    //self define for callback Manifest.permission.CODE_CAMERA
    public static final int REQUEST_PERMISSION_CAMERA = 111; //self define for callback
    private static final String MSG_PERMISSION_CAMERA = "須提供相機權限";
    //self define for callback Manifest.permission.WRITE_EXTERNAL_STORAGE
    public static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 112;
    private static final String MSG_PERMISSION_WRITE_EXTERNAL_STORAGE = "儲存照片須提供寫入儲存媒體權限";
    //self define for callback Manifest.permission.ACCESS_FINE_LOCATION
    public static final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 113;
    private static final String MSG_PERMISSION_ACCESS_FINE_LOCATION = "請允許定位權限以取得正常功能";

    private static Map<String, PermissionRequestPrams> PermissionRequestPrasHashMap = new HashMap<>();
    private static class PermissionRequestPrams {
        int RequestCode;
        String Rationale;
        PermissionRequestPrams(int RequestCode, String Rationale){
            this.RequestCode = RequestCode;
            this.Rationale = Rationale;
        }
    }
    static {
        PermissionRequestPrasHashMap.put(Manifest.permission.CAMERA,
                new PermissionRequestPrams(REQUEST_PERMISSION_CAMERA,
                        MSG_PERMISSION_CAMERA));
        PermissionRequestPrasHashMap.put(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                new PermissionRequestPrams(REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE,
                        MSG_PERMISSION_WRITE_EXTERNAL_STORAGE));
        PermissionRequestPrasHashMap.put(Manifest.permission.ACCESS_FINE_LOCATION,
                new PermissionRequestPrams(REQUEST_PERMISSION_ACCESS_FINE_LOCATION,
                        MSG_PERMISSION_ACCESS_FINE_LOCATION));
    }

    private Activity activity=null;
    private Fragment fragment=null;
    public PermissionController(@Nullable Activity activity1, @Nullable Fragment fragment1){
        fragment = fragment1;
        if(fragment!=null) {
            activity = fragment.getActivity();
        } else {
            activity = activity1;
        }
    }


    //android 6.0  要取得相關權限才可以執行，否則會crash
    public boolean checkPermission(final String permission){
        if(activity==null) {
            Log.e(LOG_TAG,"activity is null return false;");
            return false;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            if (activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                requestPermission(permission);
                return false;
            }
        }else{
            return true;
        }
    }
    @TargetApi(23)
    private void requestPermission(final String permission){
        if(activity.shouldShowRequestPermissionRationale(permission)){
            new AlertDialog.Builder(activity)
                    .setTitle(MSG_PERMISSION_CONFIRM)
                    .setMessage(PermissionRequestPrasHashMap.get(permission).Rationale)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            doRequestPermission(permission);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create().show();
        }else{
            doRequestPermission(permission);
        }
    }
    @TargetApi(23)
    private void doRequestPermission(final String permission){
        Log.d(LOG_TAG,"requestPermission permission="+permission);
        if (fragment!=null){
            // no issue on 23
            fragment.requestPermissions(new String[]{permission}, PermissionRequestPrasHashMap.get(permission).RequestCode);
        } else {
            activity.requestPermissions(new String[]{permission}, PermissionRequestPrasHashMap.get(permission).RequestCode);
        }
    }
}
