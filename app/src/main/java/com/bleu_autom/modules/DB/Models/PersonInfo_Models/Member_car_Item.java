package com.bleu_autom.modules.DB.Models.PersonInfo_Models;

import android.util.Log;
import com.bleu_autom.modules.DB.Models.Data_Model;
import org.json.JSONObject;
import java.util.HashMap;

public class Member_car_Item extends Data_Model {
    public String car_plate = "";


    @Override
    public String toString() {
        HashMap<String,String> info = new HashMap<>();
        if(!car_plate.isEmpty()) info.put("car_plate", car_plate);
        return new JSONObject(info).toString();
    }

    @Override
    public boolean setValueByJsonObject(JSONObject object) {
        if(object==null) {
            Log.d(LOG_TAG,"setValueByJsonObject object==null return false;");
            return false;
        }
        car_plate = object.optString("car_plate");
        return true;
    }
}
