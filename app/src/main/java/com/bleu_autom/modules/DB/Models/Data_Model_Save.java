package com.bleu_autom.modules.DB.Models;

import android.content.Context;
import android.util.Log;
import com.bleu_autom.modules.Controller.DataValue;
import org.json.JSONObject;

public abstract class Data_Model_Save extends Data_Model {
    public String share_key;
    public Data_Model_Save(String share_key){
        this.share_key = share_key;
    }

    public void getData(Context context){
        String getString = DataValue.getData(context, this.share_key);
        Log.d(LOG_TAG,"getData@"+this.share_key + ":" + getString);
        setValueByJsonString(getString);
    }
    public void saveData(Context context){
        String saveString=toString();
        Log.d(LOG_TAG,"saveData@"+this.share_key + ":" + saveString);
        DataValue.saveData(context, this.share_key, saveString);
    }
    public void saveNull(Context context){
        Log.d(LOG_TAG,"saveNull"+this.share_key);
        DataValue.saveData(context, this.share_key, new JSONObject().toString());
    }
}
