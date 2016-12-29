package com.bleu_autom.modules.DB.Models.PersonInfo_Models;

import android.util.Log;

import com.bleu_autom.modules.DB.Models.Data_Model;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

public class Member_trade_item extends Data_Model {
    public String trade_id = "";
    public Member_trade_item_detail detail;

    @Override
    public String toString() {
        HashMap<String,String> info = new HashMap<>();
        if(!trade_id.isEmpty()) info.put("trade_id", trade_id);
        JSONObject jsonObject = new JSONObject(info);
        try {
            jsonObject.put("detail", new JSONObject(detail.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG,"toString ="+jsonObject.toString());
        return jsonObject.toString();
    }

    @Override
    public boolean setValueByJsonObject(JSONObject object) {
        if(object==null) {
            Log.d(LOG_TAG,"setValueByJsonObject object==null return false;");
            return false;
        }
        trade_id = object.optString("trade_id");
        detail = new Member_trade_item_detail(object.optJSONObject("detail"));
        return true;
    }
}
