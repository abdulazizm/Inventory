package com.FingerPointEngg.Labs.OpenSourceInventory;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import static java.lang.System.out;

public class OrdersFragment extends Fragment {

    static View.OnClickListener myOnClickListener;
    Bundle order_data;
    LocalDb db;
    int total_orders;

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_orders,container,false);

        RecyclerView recyclerView = rootView.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        db = new LocalDb(getContext());


//        for (int j=1;j<4;j++){
//            db.addOrderDetails("aziz",""+62632564,"100"+j,"Hunksxhad","saduyagsdyag","success","2983","yes","9280810231","order placed","late","1","70","https://cdn.thingiverse.com/renders/1f/3a/50/93/37/d35133dd391dd97c37f98904744f3d86_preview_featured.jpg","p_title");
//        }
        //total_orders = db.getTotalOrders();
        
        ArrayList<DataModel> data = new ArrayList<DataModel>();

        for (int id = 1; id <= total_orders; id++) {
            //order_data = db.getOrderDetailsBySeries(id);
            try {
                String order_id = order_data.getString("order_id");
                String order_date = order_data.getString("order_date");
                String price = order_data.getString("price");
                String status = order_data.getString("status");
                String title = order_data.getString("title");
                String imageurl = order_data.getString("imageurl");
                if (!(order_date.equals("null") && order_id.equals("null") && order_data.equals("null") && price.equals("null") && status.equals("null") && title.equals("null"))) {
                    data.add(new DataModel(title, "₹" + price, id, imageurl, order_id, status, order_date));
                }
                new OrderStatus(order_id,price ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        RecyclerView.Adapter adapter = new CustomAdapter(getContext(),data);
        recyclerView.setAdapter(adapter);

        return rootView;
    }


    class OrderStatus extends AsyncTask<String,String,String> {

        String l_orderid,l_price;

        public OrderStatus(String orderid,String price) {
            this.l_orderid = orderid;
            this.l_price = price;
        }

        @Override
        protected void onPostExecute(String jsonRead) {

            super.onPostExecute(jsonRead);

            //Toast.makeText(getContext(),""+jsonRead,Toast.LENGTH_SHORT).show();
            String status="",order_status="",l_orderid="",l_price="";
            try {
                JSONObject json = new JSONObject(jsonRead);
                //{"status":"success","order_status":"Processing","order_id":"FPE3D302623058941830","price":"FREE"}
                status = json.getString("status");
                order_status = json.getString("order_status");
                l_orderid= json.getString("order_id");
                l_price= json.getString("price");
                //Toast.makeText(getApplicationContext(), status+user+jsonread, Toast.LENGTH_SHORT).show();
            } catch (JSONException e){
                e.printStackTrace();
            }

            if(status.equals("success")){

                //db.orderStatusUpdate(l_price,l_orderid,order_status);

            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            try {
                String data = URLEncoder.encode("order_id", "UTF-8")
                        + "=" + URLEncoder.encode(l_orderid, "UTF-8");

                data += "&" + URLEncoder.encode("price", "UTF-8")
                        + "=" + URLEncoder.encode(l_price, "UTF-8");

                URL url = new URL("https://www.fpelabs.com/Android_App/FreeGifts/order_status.php");
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept-Encoding", "identity");
                urlConnection.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                writer.write(data);
                writer.flush();
                writer.close();

                // Get the server response
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line;
                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line).append("\n");
                }
                reader.close();
                return sb.toString();

            } catch (Exception e) {
                out.println(e.getMessage());
            }
            return "{status:failed}";
        }
    }

}
