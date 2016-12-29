package com.bleu_autom.modules.DB.Models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class Data_Model_List {
    public String LOG_TAG;
    protected List items = new ArrayList<>();

    public abstract Data_Model getNewItem();
    public abstract List getList();
    public String toJsonArrayString(){
        return toJSONArray().toString();
    }
    public JSONArray toJSONArray(){
        JSONArray array = new JSONArray();
        for (Object item: items) {
            try {
                array.put(new JSONObject(item.toString()));
                Log.d(LOG_TAG,"items.size()="+items.size()+" array.put toString ="+item.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d(LOG_TAG,"toString ="+array.toString());
        return array;
    }
    public boolean setValueByJsonArrayString(String jsonArrayString) {
        Log.d(LOG_TAG,"setValueByJsonArrayString jasonString="+jsonArrayString);
        if(jsonArrayString.isEmpty()) return false;
        try {
            JSONArray jsonArray = new JSONArray(jsonArrayString);
            return setValueByJSONArray(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean setValueByJSONArray(JSONArray jsonArray){
        if(jsonArray==null) {
            Log.d(LOG_TAG,"setValueByJSONArray jsonArray==null return false;");
            return false;
        }
        Log.d(LOG_TAG,"setValueByJSONArray jsonArray.length()="+jsonArray.length());
        items.clear();
        for (int i=0; i<jsonArray.length();i++){
            JSONObject object = jsonArray.optJSONObject(i);
            if(object!=null){
                Data_Model item = (Data_Model) getNewItem();
                if (item.setValueByJsonObject(object)) items.add(item);
                Log.d(LOG_TAG,"setJasonString items.add("+i+") item="+item.toString());
            }
        }
        return true;
    }
    public void clear() {
        for(Object item: items) {
            ((Data_Model)item).clear();
        }
        items.clear();
    }

}