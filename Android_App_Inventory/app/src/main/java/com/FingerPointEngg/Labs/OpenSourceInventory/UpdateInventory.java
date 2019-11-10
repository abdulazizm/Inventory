package com.FingerPointEngg.Labs.OpenSourceInventory;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import static java.lang.System.load;
import static java.lang.System.out;

public class UpdateInventory extends AppCompatActivity {

    EditText price,stock,category,status,rank,description_et,name_et,image_et;
    TextView id_et;
    LinearLayout price_l,stock_l,category_l,status_l,rank_l,id_l;
    Button add_new_b,update_b;
    ProgressDialog progressDialog;
    Bundle b_product_category;
    LocalDb db;

    //cid=256&add_new=1&product_category=0&name=biscuit&
    //image=%22https://cds.com%22&description=%22chjsj%22&stock=5&
    // price=20&status=ACTIVE&rank=2&category=Snacks&id=1

    String name,image,description,status_s,category_s;
    int cid=0,stock_i=0,price_i=0,rank_i=0,id=0,product_category=0, add_new=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_inventory);

        db = new LocalDb(getApplicationContext());

        price = findViewById(R.id.price);
        stock = findViewById(R.id.stock);
        status = findViewById(R.id.status);
        category = findViewById(R.id.category);
        rank = findViewById(R.id.rank);
        description_et = findViewById(R.id.description);
        name_et = findViewById(R.id.name);
        image_et = findViewById(R.id.image_url);
        id_et = findViewById(R.id.id_et);

        price_l = findViewById(R.id.price_l);
        stock_l = findViewById(R.id.stock_l);
        status_l = findViewById(R.id.status_l);
        category_l = findViewById(R.id.category_l);
        rank_l = findViewById(R.id.rank_l);
        id_l = findViewById(R.id.id_l);

        add_new_b = findViewById(R.id.add_new);
        update_b = findViewById(R.id.update);

        b_product_category = new Bundle();

        //name = getIntent().getExtras().getString("u_input");
        product_category = getIntent().getExtras().getInt("product_category");
        add_new = getIntent().getExtras().getInt("add_new");
        b_product_category = getIntent().getExtras();

        if(add_new==0) {
            extract_data();

            image_et.setText(image);
            description_et.setText(description);
            name_et.setText(name);
            id_et.setText(String.valueOf(id));
            add_new_b.setVisibility(View.GONE);
            update_b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateUserEdit();
                    loadData();
                }
            });

            if(product_category==0){
                hideCategoryFieldsAndFillProdData(false);
            }
            else if(product_category==1){
                hideProductFieldsAndFillCategoryData(false);
            }

        } else {
            update_b.setVisibility(View.GONE);
            add_new_b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateUserEdit();

                    loadData();
                }
            });
            id_l.setVisibility(View.GONE);

            //only hide is needed
            if(product_category==0){
                hideCategoryFieldsAndFillProdData(true);
            }
            else if(product_category==1){
                hideProductFieldsAndFillCategoryData(true);
            }
        }

    }

    //Products screen
    void hideCategoryFieldsAndFillProdData(boolean hideonly){

        rank_l.setVisibility(View.GONE);
        //EditText price,stock,category,status,rank;
        if(!hideonly) {
            price.setText(String.valueOf(price_i));
            stock.setText(String.valueOf(stock_i));
            category.setText(category_s);
            status.setText(status_s);
        }


    }

    void hideProductFieldsAndFillCategoryData(boolean hideonly){

        price_l.setVisibility(View.GONE);
        stock_l.setVisibility(View.GONE);
        status_l.setVisibility(View.GONE);
        category_l.setVisibility(View.GONE);
        if(!hideonly) rank.setText(String.valueOf(rank_i));
    }

    void loadData(){
        progressDialog = ProgressDialog.show(UpdateInventory.this, "Updating", "Your update will be live once this is done", false, true);
        new loadURLTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void extract_data(){

        cid = b_product_category.getInt("cid");
        name = b_product_category.getString("name");
        image = b_product_category.getString("image");
        description = b_product_category.getString("description");
        category_s = b_product_category.getString("category");
        status_s = b_product_category.getString("status");
        stock_i = b_product_category.getInt("stock");
        price_i = b_product_category.getInt("price");
        rank_i = b_product_category.getInt("rank");
        id = b_product_category.getInt("id");

    }

    void updateUserEdit() {

        //EditText price,stock,category,status,rank,description_et,name_et,image_et;
        b_product_category.putString("name", name_et.getText().toString().trim());
        b_product_category.putString("image", image_et.getText().toString().trim());
        b_product_category.putString("description", description_et.getText().toString().trim());

        if(product_category == 0) {
            b_product_category.putString("category", category.getText().toString().trim());
            b_product_category.putString("status", status.getText().toString().trim());
            if(stock.getText().toString().trim() != null && price.getText().toString().trim() != null) {
                b_product_category.putInt("stock", Integer.parseInt(stock.getText().toString().trim()));
                b_product_category.putInt("price", Integer.parseInt(price.getText().toString().trim()));
            }

        } else {
            if(rank.getText().toString().trim() != null)
                b_product_category.putInt("rank", Integer.parseInt(rank.getText().toString().trim()));
        }

    }

    class loadURLTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPostExecute(String jsonRead) {

            super.onPostExecute(jsonRead);
            //Toast.makeText(getApplicationContext(), jsonRead, Toast.LENGTH_SHORT).show();
            //{"status":"success","data":"updated"}
            String status="",user="";
            try {
                JSONObject json = new JSONObject(jsonRead);
                status = json.getString("status");

            } catch (JSONException e){
                e.printStackTrace();
            }

            progressDialog.dismiss();
            if(status.equals("success")){

                AlertDialog.Builder builder=new AlertDialog.Builder(UpdateInventory.this);
                builder.setTitle("Update Successful");
                builder.setMessage("Your Update is live");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            } else{

                AlertDialog.Builder builder=new AlertDialog.Builder(UpdateInventory.this);
                builder.setTitle("Update failed");
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
        protected String doInBackground(String... strings) {

            try {
                //cid=256&add_new=1&product_category=0&name=biscuit&
                //image=%22https://cds.com%22&description=%22chjsj%22&stock=5&
                // price=20&status=ACTIVE&rank=2&category=Snacks&id=1

                String data = URLEncoder.encode("cid", "UTF-8")
                        + "=" + URLEncoder.encode(String.valueOf(db.getCID()), "UTF-8");

                data += "&" + URLEncoder.encode("add_new", "UTF-8")
                        + "=" + URLEncoder.encode(String.valueOf(add_new), "UTF-8");

                data += "&" + URLEncoder.encode("product_category", "UTF-8")
                        + "=" + URLEncoder.encode(String.valueOf(product_category), "UTF-8");

                data += "&" + URLEncoder.encode("name", "UTF-8")
                        + "=" + URLEncoder.encode(b_product_category.getString("name"), "UTF-8");

                data += "&" + URLEncoder.encode("image", "UTF-8")
                        + "=" + URLEncoder.encode(b_product_category.getString("image"), "UTF-8");

                data += "&" + URLEncoder.encode("description", "UTF-8")
                        + "=" + URLEncoder.encode(b_product_category.getString("description"), "UTF-8");

                if(add_new == 0) {
                    data += "&" + URLEncoder.encode("id", "UTF-8")
                            + "=" + URLEncoder.encode(String.valueOf(id), "UTF-8");
                } else {
                    data += "&" + URLEncoder.encode("id", "UTF-8")
                            + "=" + URLEncoder.encode(String.valueOf(0), "UTF-8");
                }

                if(product_category == 1) {
                    data += "&" + URLEncoder.encode("stock", "UTF-8")
                            + "=" + URLEncoder.encode(String.valueOf(0), "UTF-8");

                    data += "&" +URLEncoder.encode("price", "UTF-8")
                            + "=" + URLEncoder.encode(String.valueOf(0), "UTF-8");

                    data += "&" + URLEncoder.encode("status", "UTF-8")
                            + "=" + URLEncoder.encode("INACTIVE", "UTF-8");

                    data += "&" + URLEncoder.encode("rank", "UTF-8")
                            + "=" + URLEncoder.encode(String.valueOf(b_product_category.getInt("rank")), "UTF-8");

                    data += "&" + URLEncoder.encode("category", "UTF-8")
                            + "=" + URLEncoder.encode("NA", "UTF-8");
                } else {
                    data += "&" + URLEncoder.encode("stock", "UTF-8")
                            + "=" + URLEncoder.encode(String.valueOf(b_product_category.getInt("stock")), "UTF-8");

                    data += "&" +URLEncoder.encode("price", "UTF-8")
                            + "=" + URLEncoder.encode(String.valueOf(b_product_category.getInt("price")), "UTF-8");

                    data += "&" + URLEncoder.encode("status", "UTF-8")
                            + "=" + URLEncoder.encode(b_product_category.getString("status"), "UTF-8");

                    data += "&" + URLEncoder.encode("rank", "UTF-8")
                            + "=" + URLEncoder.encode(String.valueOf(0), "UTF-8");

                    data += "&" + URLEncoder.encode("category", "UTF-8")
                            + "=" + URLEncoder.encode(b_product_category.getString("category"), "UTF-8");
                }


                URL url = new URL("https://www.fingerpointengg.com/Android_App/InventoryManagement/manage_inventory.php");
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
                return e.getMessage();
            }
            //return "{\"status\":\"failed\"}";
        }
    }
}
