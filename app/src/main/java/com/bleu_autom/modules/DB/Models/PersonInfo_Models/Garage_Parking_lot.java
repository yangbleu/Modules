package com.bleu_autom.modules.DB.Models.PersonInfo_Models;

import android.util.Log;
import com.bleu_autom.modules.DB.Models.Data_Model;
import org.json.JSONObject;
import java.util.HashMap;

public class Garage_Parking_lot extends Data_Model {
    public String garage_id="";

    @Override
    public String toString() {
        HashMap<String,String> info = new HashMap<>();
        if(!garage_id.isEmpty()) info.put("garage_id", garage_id);
        return new JSONObject(info).toString();
    }

    @Override
    public boolean setValueByJsonObject(JSONObject object) {
        if(object==null) {
            Log.d(LOG_TAG,"setValueByJsonObject object==null return false;");
            return false;
        }
        garage_id = object.optString("garage_id");
        return true;
    }
}
