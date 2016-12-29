package com.bleu_autom.modules.DB;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class GarageDB {
    private static String LOG_TAG = "";
    // LOG_TAG = this.getClass().getSimpleName();
    //Singleton Mode
    private static GarageDB instance = null;
    public static GarageDB getInstance() {
        if (instance == null) {
            instance = new GarageDB();
            LOG_TAG = instance.getClass().getSimpleName();
            Log.d(LOG_TAG,"new "+LOG_TAG);
        }
        return instance;
    }
    private static final String Para_Garage_List = "Para_Garage_List";

    private Map<String,Context> ContextMap = new HashMap<>();
    private Map<String,OnFinishListener> OnFinishListenerMap = new HashMap<>();

    public void do_garage_list(final Context context, final OnFinishListener onFinishListener, final String key_word){
        String token = String.valueOf(System.currentTimeMillis());
        ContextMap.put(token,context);
        OnFinishListenerMap.put(token,onFinishListener);
        new PersonInfoTask().execute(Para_Garage_List, token, key_word);
    }

    public class PersonInfoTask extends AsyncTask<String, Void, String> {
        private Context context;
        private JSONObject jsonObject;
        private String Para, token;
        private String status="", message="";
        private OnFinishListener onFinishListener;
        @Override
        protected String doInBackground(String... params) {
            Para = params[0];
            token = params[1];
            Log.d(LOG_TAG,"PersonInfoTask Para="+Para);
            context = ContextMap.get(token);
            ContextMap.remove(token);
            onFinishListener = OnFinishListenerMap.get(token);
            OnFinishListenerMap.remove(token);
            switch (Para) {
                case Para_Garage_List:
                    //Post_garage_list post_garage_list = new Post_garage_list();
                    //return post_garage_list.sent(context,params[2]);
            }
            return "";
        }

        @Override
        protected void onPostExecute(String json_message) {
            //return on activity is finished
            if (context==null||((context instanceof Activity)&&((Activity) context).isFinishing())) {
                Log.d(LOG_TAG,"Escape for activity.isFinishing() @"+Para);
                return;
            }
            Log.d(LOG_TAG,"PersonInfoTask@"+Para+" json_message="+json_message);
            try {
                jsonObject = new JSONObject(json_message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(jsonObject==null) {
                onFinishListener.OnReceived(false,"No Response",0);
                return;
            }
            status = jsonObject.optString("status",null);
            message = jsonObject.optString("message");
            if (status!=null && status.equals("success")) {
                JSONArray response_array = jsonObject.optJSONArray("response");
                switch (Para) {
                    case Para_Garage_List:
                        if (response_array!=null) { }
                        break;
                }
                onFinishListener.OnReceived(true,message,0);
            } else {
                onFinishListener.OnReceived(false,message,0);
            }
        }
    }
}
