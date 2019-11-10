package com.FingerPointEngg.Labs.OpenSourceInventory;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import in.juspay.godel.jseval.WebViewWrapper;

public class SettingsFragment extends Fragment  {


    CardView card_profile,card_device,card_logout,card_control,card_help,card_inventory;
    TextView user_name,comp_name,comp_id;
    LocalDb db;
    Bundle user_data;

    @SuppressLint({"InflateParams", "SetTextI18n"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        card_profile = v.findViewById(R.id.card_profile);
        card_device = v.findViewById(R.id.card_device);
        card_logout = v.findViewById(R.id.card_logout);
        card_control = v.findViewById(R.id.card_control);
        card_help = v.findViewById(R.id.card_help);
        user_name = v.findViewById(R.id.user_name);
        comp_name = v.findViewById(R.id.comp_name);
        comp_id = v.findViewById(R.id.comp_id);
        card_inventory = v.findViewById(R.id.card_inventory);

        db = new LocalDb(getContext());
//        user_data = new Bundle();
//        user_data = db.getUserDetails();
        user_name.setText(db.getUsername());
        comp_id.setText("ID: "+db.getCID());
        comp_name.setText("Company: "+db.getCompanyName());

//        if(!user_data.getString("phone").equals("null"))
//            user_devices.setText(user_data.getString("phone"));
//        else
//            user_devices.setText("Click here !!");
//        card_profile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent logout = new Intent(getContext(), AddUserDeliveryData.class);
//                startActivity(logout);
//            }
//        });
//
//        card_device.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent logout = new Intent(getContext(), AddUserDeliveryData.class);
//                startActivity(logout);
//            }
//        });
//
//        card_control.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent cs = new Intent(getContext(), ComingSoon.class);
//                startActivity(cs);
//            }
//        });

        card_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cs = new Intent(getContext(), ComingSoon.class);
                startActivity(cs);
            }
        });


        card_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                //builder.setCancelable(true);
                builder.setTitle("Logout");
                builder.setMessage("You can't manage inventory until you login again. Are you sure to logout and manage later? ");
                builder.setNegativeButton("I will manage later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.logout();
                        dialog.cancel();
                        Intent logout = new Intent(getContext(), SplashScreen.class);
                        startActivity(logout);
                    }
                });
                builder.setPositiveButton("Keep managing",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setIcon(getResources().getDrawable(R.drawable.ic_card_giftcard_black_24dp ));
                builder.show();

            }
        });

        card_inventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inv_mgmt = new Intent(getContext(), Inventory.class);
                startActivity(inv_mgmt);
            }
        });
        return v;
    }

}
