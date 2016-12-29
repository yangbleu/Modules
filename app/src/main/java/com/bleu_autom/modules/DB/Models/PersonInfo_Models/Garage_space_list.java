package com.bleu_autom.modules.DB.Models.PersonInfo_Models;

import android.util.Log;
import com.bleu_autom.modules.DB.Models.Data_Model_List_Save;
import java.util.List;

public class Garage_space_list extends Data_Model_List_Save {
    public Garage_space_list(String share_key) {
        super(share_key);
        LOG_TAG = this.getClass().getSimpleName();
        Log.d(LOG_TAG,"LOG_TAG");
    }

    @Override
    public Garage_space_data getNewItem() {
        return new Garage_space_data();
    }

    @Override
    public List<Garage_space_data> getList() {
        return items;
    }
}
