package com.FingerPointEngg.Labs.OpenSourceInventory;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private ArrayList<DataModel> dataSet;
    private Context context;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView p_title,p_price,order_id,order_status,last_update_time;
        ImageView imageViewIcon;


        public MyViewHolder(View itemView) {
            super(itemView);
            this.p_title = (TextView) itemView.findViewById(R.id.p_title);
            this.p_price = (TextView) itemView.findViewById(R.id.p_price);
            this.order_id = (TextView) itemView.findViewById(R.id.order_id);
            this.imageViewIcon = (ImageView) itemView.findViewById(R.id.imageView);
            this.order_status = (TextView) itemView.findViewById(R.id.order_status);
            this.last_update_time = (TextView) itemView.findViewById(R.id.last_update_time);
        }
    }

    public CustomAdapter(Context context, ArrayList<DataModel> data) {
        this.context = context; this.dataSet = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_layout, parent, false);

        view.setOnClickListener(OrdersFragment.myOnClickListener);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        TextView p_title = holder.p_title;
        TextView p_price = holder.p_price;
        TextView order_id = holder.order_id;
        TextView order_status = holder.order_status;
        TextView last_update_time = holder.last_update_time;
        //ImageView imageView = holder.imageViewIcon;

        Bundle data = new Bundle();
        data=dataSet.get(listPosition).getOrderDetails();
        p_title.setText(data.getString("p_title"));
        p_price.setText(data.getString("p_price"));
        order_id.setText(data.getString("order_id"));
        order_status.setText(data.getString("order_status"));
        last_update_time.setText(data.getString("last_update_time"));
//        imageView.setImageResource(dataSet.get(listPosition).getImage());
        Picasso.with(context).load(data.getString("imageurl")).resize(300,300).into(holder.imageViewIcon);

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
