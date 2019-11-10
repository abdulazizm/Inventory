package com.FingerPointEngg.Labs.OpenSourceInventory;

import android.os.Bundle;

public class DataModel {

    int id_;
    Bundle data;
    //int image;

    public DataModel(String p_title, String p_price, int id_, String imageurl, String order_id, String order_status, String last_update_time) {
        data = new Bundle();
        data.putString("p_title",p_title);
        data.putString("p_price",p_price);
        data.putString("order_id",order_id);
        data.putString("order_status",order_status);
        data.putString("last_update_time",last_update_time);
        data.putString("imageurl",imageurl);
        this.id_ = id_;
        //this.image=image;
    }

    public Bundle getOrderDetails() {

        return data;
    }

//    public String getImage() {
//
//        return image;
//    }

    public int getId() {

        return id_;
    }

}
