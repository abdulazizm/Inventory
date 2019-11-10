package com.FingerPointEngg.Labs.OpenSourceInventory;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hariofspades.incdeclibrary.IncDecCircular;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import static java.lang.System.out;

public class ProductPage extends AppCompatActivity {

    int prod_id;
    String price,title,imageurl,quantity,l_descrip;
    Bundle data;
    String[] items = new String[]{"1", "2", "3"};
    TextView p_title,p_price, delivery, description;
    ImageView image;
    Button buy_now,add_to_cart;
    ProgressDialog progressDialog;
    //Spinner dropdown;
    LocalDb db;
    IncDecCircular incdec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_page);
        db=new LocalDb(getApplicationContext());

        incdec = findViewById(R.id.incdec);
        progressDialog = ProgressDialog.show(ProductPage.this, "Loading Product Contents", null, false, true);

        final Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        }, 1000);

        image = (ImageView) findViewById(R.id.p_image);

//        dropdown = findViewById(R.id.quan);
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
//        dropdown.setAdapter(adapter);

        data = getIntent().getExtras();
        if(data.getString("pid")!=null)
        prod_id = Integer.parseInt(data.getString("pid"));
        price = data.getString("price");
        title = data.getString("title");
        imageurl = data.getString("imageurl");
        l_descrip = data.getString("description");

        buy_now = findViewById(R.id.buynow);
        add_to_cart = findViewById(R.id.add_to_cart);
        p_title = findViewById(R.id.title);
        p_price = findViewById(R.id.price);
        delivery = findViewById(R.id.delivery);
        description = findViewById(R.id.description);

        p_title.setText(title);
        if(!price.equals("FREE"))
            p_price.setText("₹"+price);
        else
            p_price.setText(price);
//        if(!db.checkNewUser()) {
//            delivery.setText("₹50");
//        }else{
            delivery.setText("FREE");
//        }
        description.setText(l_descrip);

        new LoadImage(image).execute(imageurl);

        buy_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent product = new Intent(getApplicationContext(), Inventory.class);
//                //quantity = dropdown.getSelectedItem().toString();
//                data.putString("quantity",quantity);
//                product.putExtras(data);
                startActivity(product);



            }
        });

        add_to_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent product = new Intent(getApplicationContext(), BuyNow.class);
//                //quantity = dropdown.getSelectedItem().toString();
//                data.putString("quantity",quantity);
//                product.putExtras(data);
//                startActivity(product);

                quantity = incdec.getValue();
                progressDialog = ProgressDialog.show(ProductPage.this, "Updating", null, false, true);
                new newSaleTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            }
        });


        incdec.setConfiguration(LinearLayout.HORIZONTAL,IncDecCircular.TYPE_INTEGER,
                IncDecCircular.DECREMENT,IncDecCircular.INCREMENT);
        incdec.setupValues(0,100,1,0);
        incdec.enableLongPress(false,false,500);

    }


    @SuppressLint("StaticFieldLeak")
    class newSaleTask extends AsyncTask<String, String, String> {


        @Override
        protected void onPostExecute(String jsonRead) {

            super.onPostExecute(jsonRead);
            //Toast.makeText(getApplicationContext(), jsonRead, Toast.LENGTH_SHORT).show();
            String status = "", data = "";
            try {
                JSONObject json = new JSONObject(jsonRead);
                status = json.getString("status");
                data = json.getString("data");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            progressDialog.dismiss();
            if (status.equals("success") && data.equals("uploaded")) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ProductPage.this);
                builder.setTitle("Sales Updated Successfully");
                builder.setMessage("Data will update the graph");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(ProductPage.this);
                builder.setTitle("Sales Update failed");
                builder.setMessage("Network Connectivity Error. Please try again later");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground (String...strings){


            try {

                //int prod_id;
                //    String price,title,imageurl,quantity,l_descrip;

                //return "{\"status\":\"success\",\"data\":\"uploaded\"}";
                String data = URLEncoder.encode("cid", "UTF-8")
                        + "=" + URLEncoder.encode(String.valueOf(db.getCID()), "UTF-8");

                data += "&" + URLEncoder.encode("prod_id", "UTF-8")
                        + "=" + URLEncoder.encode(String.valueOf(prod_id), "UTF-8");

                data += "&" + URLEncoder.encode("data", "UTF-8")
                        + "=" + URLEncoder.encode("xHzy5InventorySec", "UTF-8");

                data += "&" + URLEncoder.encode("prod_price", "UTF-8")
                        + "=" + URLEncoder.encode(price, "UTF-8");

                data += "&" + URLEncoder.encode("quantity", "UTF-8")
                        + "=" + URLEncoder.encode(quantity, "UTF-8");

                URL url = new URL("https://www.fingerpointengg.com/Android_App/InventoryManagement/new_order.php");
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
            return "{\"status\":\"failed\"}";
        }
    }
}
