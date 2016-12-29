package com.bleu_autom.modules.DB.Models.PersonInfo_Models;

import android.util.Log;
import com.bleu_autom.modules.DB.Models.Data_Model_Save;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

public class Local_temp_data extends Data_Model_Save {
    public String user_photo_uri = "";
    public Garage_space_list floor_sensor_data = new Garage_space_list("floor_sensor_data");

    public Local_temp_data(String share_key) {
        super(share_key);
        LOG_TAG = this.getClass().getSimpleName();
        Log.d(LOG_TAG,"LOG_TAG");
    }

    @Override
    public String toString() {
        HashMap<String,String> info = new HashMap<>();
        info.put("user_photo_uri", user_photo_uri);

        JSONObject jsonObject = new JSONObject(info);
        try {
            jsonObject.put("floor_sensor_data", floor_sensor_data.toJSONArray());
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
        user_photo_uri = object.optString("user_photo_uri");

        floor_sensor_data.setValueByJSONArray(object.optJSONArray("floor_sensor_data"));

        return true;
    }
}
