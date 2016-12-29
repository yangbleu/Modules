package com.bleu_autom.modules.DB.Models.PersonInfo_Models;

import org.json.JSONException;
import org.json.JSONObject;

public class Member_trade_item_detail {
    public Garage_Parking_lot parking_lot = new Garage_Parking_lot();
    public Reserve_data reserve_data = new Reserve_data();
    public Enter_parking_data enter_parking_data = new Enter_parking_data();
    public Member_trade_item_detail(JSONObject object) {
        if(object==null) return;
        parking_lot.setValueByJsonObject(object.optJSONObject("parking_lot"));
        reserve_data.setValueByJsonObject(object.optJSONObject("reserve_data"));
        enter_parking_data.setValueByJsonObject(object.optJSONObject("enter_parking_data"));
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("parking_lot", new JSONObject(parking_lot.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            jsonObject.put("reserve_data", new JSONObject(reserve_data.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            jsonObject.put("enter_parking_data", new JSONObject(enter_parking_data.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
