package com.bleu_autom.modules.DB.Models;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class Data_Model {
    public String LOG_TAG;
    public abstract String toString();
    public boolean setValueByJsonString(String jasonString) {
        try {
            JSONObject object = new JSONObject(jasonString);
            return setValueByJsonObject(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG,"setValueByJsonString return false;");
        return false;
    }
    public abstract boolean setValueByJsonObject(JSONObject object);
    public void clear() {
        setValueByJsonObject(new JSONObject());
    }
}