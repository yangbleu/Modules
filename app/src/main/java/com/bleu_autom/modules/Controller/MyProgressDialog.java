package com.bleu_autom.modules.Controller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Sean on 2016/9/5.
 * Modified by George on 2016/12/22
 */
public class MyProgressDialog {
    private static String LOG_TAG = "";
    // LOG_TAG = this.getClass().getSimpleName();

    public static final int TIMEOUT = 20;//sec

    private Context context;
    private static ProgressDialog progressDialog = null;
    private static Handler handler = null;
    private static MyRunnable runnable = null;

    private OnTimeoutListener myTimeoutListener;
    private String strTimeoutToast= "";

    public void start(Context context1, String action, int sec) {
        start(context1, String.format("%s中", action), sec, String.format("%s逾時", action));
    }
    public void start(Context context1, String message, int sec, String strTimeoutToast1){
        LOG_TAG = this.getClass().getSimpleName();
        dismiss();

        context = context1;
        strTimeoutToast = strTimeoutToast1;
        progressDialog = ProgressDialog.show(context, "", message);

        //handler
        if(sec > 0){
            Log.d(LOG_TAG, "Work sec  = "+ sec);
            if(runnable != null){
                handler.removeCallbacks(runnable);
                runnable = null;
            }
            runnable = new MyRunnable();
            handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(runnable, (long)sec*1000);
        }
        Log.d(LOG_TAG, "Progress Dialog Start");
    }

    public void dismiss(){
        if(runnable != null && handler!=null){
            handler.removeCallbacks(runnable);
            runnable = null;
        }
        if(progressDialog != null){
            if (context instanceof Activity) {
                if (!((Activity) context).isFinishing())
                    progressDialog.dismiss();
            } else {
                progressDialog.dismiss();
            }
            progressDialog = null;
        }
        context = null;
        strTimeoutToast = "";
        Log.d(LOG_TAG, "Progress Dialog Stop");
    }

    public void setCanceledOnTouchOutside(boolean isCancelOnTouchOutside){
        if(progressDialog != null) {
            progressDialog.setCanceledOnTouchOutside(isCancelOnTouchOutside);
        }
    }

    /**
     * Timeout
     */
    private class MyRunnable implements Runnable {
        @Override
        public void run() {
            if(!strTimeoutToast.equals("")){
                if (context instanceof Activity) {
                    if (!((Activity) context).isFinishing())
                        Toast.makeText(context, strTimeoutToast, Toast.LENGTH_SHORT).show();
                }
            }
            if (myTimeoutListener != null){
                Log.d(LOG_TAG, "Timeout Listener != null");
                myTimeoutListener.OnTimeout();
            }
            Log.d(LOG_TAG, "( Runnable time out ) dismiss!!");
            dismiss();
        }
    }

    /**
     * 回調介面
     **/
    public interface OnTimeoutListener {
        void OnTimeout();
    }
    public void setOnTimeoutListener(OnTimeoutListener timeoutListener) {
        this.myTimeoutListener = timeoutListener;
    }
}
