package com.bleu_autom.modules.DB.Models.PersonInfo_Models;

import android.util.Log;

import com.bleu_autom.modules.DB.Models.Data_Model_Save;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Member_data extends Data_Model_Save {
    public String access_token = "";
    public List<String> oauth_list = new ArrayList<>();

    public Member_data(String share_key) {
        super(share_key);
        LOG_TAG = this.getClass().getSimpleName();
        Log.d(LOG_TAG,"LOG_TAG");
    }

    @Override
    public String toString() {
        HashMap<String,String> info = new HashMap<>();
        if(!access_token.isEmpty()) info.put("access_token", access_token);
        JSONObject object = new JSONObject(info);
        try {
            object.put("oauth_list",new JSONArray(oauth_list));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    @Override
    public boolean setValueByJsonObject(JSONObject object) {
        if(object==null) {
            Log.d(LOG_TAG,"setValueByJsonObject object==null return false;");
            return false;
        }
        access_token = object.optString("access_token");
        JSONArray json_oauth_list = object.optJSONArray("oauth_list");
        if(json_oauth_list!=null){
            Log.d(LOG_TAG,"json_oauth_list="+json_oauth_list.toString());
            oauth_list.clear();
            for (int i=0; i < json_oauth_list.length() ;i++){
                oauth_list.add(json_oauth_list.optString(i));
                Log.d(LOG_TAG,"oauth_list.add : json_oauth_list.optString("+i+")="+json_oauth_list.optString(i));
            }
        }
        return true;
    }
}
