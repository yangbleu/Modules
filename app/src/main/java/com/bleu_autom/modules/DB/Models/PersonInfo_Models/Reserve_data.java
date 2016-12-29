package com.bleu_autom.modules.DB.Models.PersonInfo_Models;

import android.util.Log;
import com.bleu_autom.modules.DB.Models.Data_Model;
import org.json.JSONObject;
import java.util.HashMap;

public class Reserve_data extends Data_Model {
    public String reserve_id="";

    @Override
    public String toString() {
        HashMap<String,String> info = new HashMap<>();
        if(!reserve_id.isEmpty()) info.put("reserve_id", reserve_id);
        return new JSONObject(info).toString();
    }

    @Override
    public boolean setValueByJsonObject(JSONObject object) {
        if(object==null) {
            Log.d(LOG_TAG,"setValueByJsonObject object==null return false;");
            return false;
        }
        reserve_id = object.optString("reserve_id");
        return true;
    }
}
