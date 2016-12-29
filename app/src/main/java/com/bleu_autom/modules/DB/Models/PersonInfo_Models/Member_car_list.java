package com.bleu_autom.modules.DB.Models.PersonInfo_Models;

import android.util.Log;
import com.bleu_autom.modules.DB.Models.Data_Model_List_Save;
import java.util.List;

public class Member_car_list extends Data_Model_List_Save {
    public Member_car_list(String share_key) {
        super(share_key);
        LOG_TAG = this.getClass().getSimpleName();
        Log.d(LOG_TAG,"LOG_TAG");
    }

    @Override
    public Member_car_Item getNewItem() {
        return new Member_car_Item();
    }

    @Override
    public List<Member_car_Item> getList() {
        return items;
    }
}
