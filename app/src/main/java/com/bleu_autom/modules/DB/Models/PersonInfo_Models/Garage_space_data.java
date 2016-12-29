package com.bleu_autom.modules.DB.Models.PersonInfo_Models;

import android.util.Log;
import com.bleu_autom.modules.DB.Models.Data_Model;
import org.json.JSONObject;
import java.util.HashMap;


public class Garage_space_data extends Data_Model {

    public String space_name="";

    @Override
    public String toString() {
        HashMap<String,String> info = new HashMap<>();
        if(!space_name.isEmpty()) info.put("space_name", space_name);
        return new JSONObject(info).toString();
    }

    @Override
    public boolean setValueByJsonObject(JSONObject object) {
        if(object==null) {
            Log.d(LOG_TAG,"setValueByJsonObject object==null return false;");
            return false;
        }
        space_name = object.optString("space_name");
        return true;
    }
}
