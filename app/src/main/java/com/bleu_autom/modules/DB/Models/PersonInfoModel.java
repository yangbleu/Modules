package com.bleu_autom.modules.DB.Models;

import android.content.Context;

import com.bleu_autom.modules.DB.Models.PersonInfo_Models.Current_member_trade;
import com.bleu_autom.modules.DB.Models.PersonInfo_Models.Local_temp_data;
import com.bleu_autom.modules.DB.Models.PersonInfo_Models.Member_car_list;
import com.bleu_autom.modules.DB.Models.PersonInfo_Models.Member_data;

public class PersonInfoModel {
    public static Member_data               member_data = new Member_data("member_data");
    public static Member_car_list           member_car = new Member_car_list("member_car");
    public static Local_temp_data           local_data = new Local_temp_data("local_data");
    public static Current_member_trade      current_member_trade = new Current_member_trade("current_member_trade");

    public static void getAllData(Context context){
        newAll();
        member_data.getData(context);
        member_car.getData(context);
        local_data.getData(context);
        current_member_trade.getData(context);
    }

    public static void clearAllData(Context context){
        member_data.saveNull(context);
        member_car.saveNull(context);
        local_data.saveNull(context);
        current_member_trade.saveNull(context);
        newAll();
    }

    private static void newAll(){
        member_data = new Member_data("member_data");
        member_car = new Member_car_list("member_car");
        local_data = new Local_temp_data("local_data");
        current_member_trade = new Current_member_trade("current_member_trade");
    }

}