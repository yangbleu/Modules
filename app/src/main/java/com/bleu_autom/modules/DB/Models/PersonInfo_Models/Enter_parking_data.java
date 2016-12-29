package com.bleu_autom.modules.DB.Models.PersonInfo_Models;

import android.util.Log;
import com.bleu_autom.modules.DB.Models.Data_Model;
import org.json.JSONObject;
import java.util.HashMap;

public class Enter_parking_data extends Data_Model {
    public String enter_id="";

    @Override
    public String toString() {
        HashMap<String,String> info = new HashMap<>();
        if(!enter_id.isEmpty()) info.put("enter_id", enter_id);
        return new JSONObject(info).toString();
    }

    @Override
    public boolean setValueByJsonObject(JSONObject object) {
        if(object==null) {
            Log.d(LOG_TAG,"setValueByJsonObject object==null return false;");
            return false;
        }
        enter_id = object.optString("enter_id");
        return true;
    }
}
