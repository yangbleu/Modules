package com.bleu_autom.modules.DB.Models.PersonInfo_Models;

import android.util.Log;

import com.bleu_autom.modules.DB.Models.Data_Model_Save;
import org.json.JSONObject;
import java.util.HashMap;

public class Current_member_trade  extends Data_Model_Save {
    //Member_trade_item
    public String trade_id = "";
    public CarPositionEnum carPosition = CarPositionEnum.outGarage;
    public enum CarPositionEnum {
        nearGarage,inGarage,parking,outGarage
    }
    public Current_member_trade(String share_key) {
        super(share_key);
    }

    @Override
    public String toString() {
        HashMap<String,String> info = new HashMap<>();
        if(!trade_id.isEmpty()) info.put("trade_id", trade_id);

        if(carPosition!=null) info.put("carPosition", carPosition.name());
        return new JSONObject(info).toString();
    }

    @Override
    public boolean setValueByJsonObject(JSONObject object) {
        if(object==null) {
            Log.d(LOG_TAG,"setValueByJsonObject object==null return false;");
            return false;
        }
        trade_id = object.optString("trade_id");
        String carPositionStr = object.optString("carPosition");
        try {
            if(!carPositionStr.isEmpty()){
                carPosition = CarPositionEnum.valueOf(carPositionStr);
            } else {
                //carPosition = CarPositionEnum.nearGarage;
            }
        } catch (IllegalArgumentException e) {
            //carPosition = CarPositionEnum.nearGarage;
            e.printStackTrace();
        }

        return true;
    }

}
